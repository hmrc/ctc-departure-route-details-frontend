/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.journeyDomain.transit

import config.Constants.DeclarationType._
import config.PhaseConfig
import models.journeyDomain._
import models.{Mode, UserAnswers}
import pages.external.{DeclarationTypePage, OfficeOfDepartureInCL112Page, OfficeOfDeparturePage}
import pages.routing.{OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import pages.sections.routing.CountriesOfRoutingSection
import pages.sections.transit.OfficesOfTransitSection
import pages.transit.{AddOfficeOfTransitYesNoPage, T2DeclarationTypeYesNoPage}
import play.api.mvc.Call

case class TransitDomain(
  isT2DeclarationType: Option[Boolean],
  officesOfTransit: Option[OfficesOfTransitDomain]
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    OfficesOfTransitSection.route(userAnswers, mode)
}

object TransitDomain {

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransitDomain] = {

    val officesOfTransitReader: Read[Option[OfficesOfTransitDomain]] =
      OfficesOfTransitDomain.userAnswersReader.toOption

    lazy val addOfficesOfTransitReader: Read[Option[OfficesOfTransitDomain]] =
      AddOfficeOfTransitYesNoPage.filterOptionalDependent(identity)(OfficesOfTransitDomain.userAnswersReader)

    (
      OfficeOfDeparturePage.reader,
      OfficeOfDepartureInCL112Page.reader,
      OfficeOfDestinationPage.reader,
      OfficeOfDestinationInCL112Page.reader
    ).to {
      case (officeOfDeparture, officeOfDepartureInCL112, officeOfDestination, officeOfDestinationInCL112) =>
        def countriesOfRoutingReader(isT2DeclarationType: Option[Boolean]): Read[TransitDomain] = {
          val officesOfTransit: Read[Option[OfficesOfTransitDomain]] =
            if (officeOfDepartureInCL112 || officeOfDestinationInCL112) {
              officesOfTransitReader
            } else {
              CountriesOfRoutingSection.anyCountriesOfRoutingInCL112.to {
                case true  => officesOfTransitReader
                case false => addOfficesOfTransitReader
              }
            }

          officesOfTransit.map(TransitDomain(isT2DeclarationType, _))
        }

        if (officeOfDepartureInCL112 && officeOfDestinationInCL112 && officeOfDeparture.countryId == officeOfDestination.countryId) {
          addOfficesOfTransitReader.map(TransitDomain.apply(None, _))
        } else {
          DeclarationTypePage.reader.to {
            case T2 =>
              officesOfTransitReader.map(TransitDomain(None, _))
            case T =>
              T2DeclarationTypeYesNoPage.reader.to {
                case true =>
                  officesOfTransitReader.map(TransitDomain(Some(true), _))
                case false =>
                  countriesOfRoutingReader(Some(false))
              }
            case _ =>
              countriesOfRoutingReader(None)
          }
        }
    }
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}
