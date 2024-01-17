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
import models.domain._
import models.journeyDomain.transit.TransitDomain.OfficesOfTransit
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess, Stage}
import models.{Index, Mode, RichJsArray, UserAnswers}
import pages.external.{DeclarationTypePage, OfficeOfDepartureInCL112Page, OfficeOfDeparturePage}
import pages.routing.{OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import pages.sections.routing.CountriesOfRoutingSection
import pages.sections.transit.OfficesOfTransitSection
import pages.transit.{AddOfficeOfTransitYesNoPage, T2DeclarationTypeYesNoPage}
import play.api.mvc.Call

case class TransitDomain(
  isT2DeclarationType: Option[Boolean],
  officesOfTransit: OfficesOfTransit
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(controllers.transit.routes.AddAnotherOfficeOfTransitController.onPageLoad(userAnswers.lrn, mode))
}

object TransitDomain {

  type OfficesOfTransit = Seq[OfficeOfTransitDomain]

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransitDomain] = {

    val officesOfTransitReader: Read[OfficesOfTransit] =
      OfficesOfTransitSection.arrayReader.apply(_).flatMap {
        case ReaderSuccess(x, pages) if x.isEmpty =>
          OfficeOfTransitDomain.userAnswersReader(Index(0)).map(Seq(_)).apply(pages)
        case ReaderSuccess(x, pages) =>
          x.traverse[OfficeOfTransitDomain](OfficeOfTransitDomain.userAnswersReader(_).apply(_)).apply(pages)
      }

    lazy val addOfficesOfTransitReader: Read[OfficesOfTransit] =
      AddOfficeOfTransitYesNoPage
        .filterOptionalDependent(identity)(officesOfTransitReader)
        .apply(_)
        .map(_.to(_.getOrElse(Nil)))

    (
      OfficeOfDeparturePage.reader,
      OfficeOfDepartureInCL112Page.reader,
      OfficeOfDestinationPage.reader,
      OfficeOfDestinationInCL112Page.reader
    ).pmap {
      case (officeOfDeparture, officeOfDepartureInCL112, officeOfDestination, officeOfDestinationInCL112) =>
        pages =>
          def countriesOfRoutingReader(isT2DeclarationType: Option[Boolean]): Read[TransitDomain] = pages => {
            val officesOfTransit = if (officeOfDepartureInCL112 || officeOfDestinationInCL112) {
              officesOfTransitReader(pages)
            } else {
              CountriesOfRoutingSection.anyCountriesOfRoutingInCL112(pages).flatMap {
                case ReaderSuccess(true, pages)  => officesOfTransitReader(pages)
                case ReaderSuccess(false, pages) => addOfficesOfTransitReader(pages)
              }
            }

            officesOfTransit.map(_.to(TransitDomain(isT2DeclarationType, _)))
          }

          if (officeOfDepartureInCL112 && officeOfDestinationInCL112 && officeOfDeparture.countryCode == officeOfDestination.countryCode) {
            addOfficesOfTransitReader.apply(pages).map(_.to(TransitDomain(None, _)))
          } else {
            DeclarationTypePage.reader.apply(pages).flatMap {
              case ReaderSuccess(T2, pages) =>
                officesOfTransitReader(pages).map(_.to(TransitDomain(None, _)))
              case ReaderSuccess(T, pages) =>
                T2DeclarationTypeYesNoPage.reader.apply(pages).flatMap {
                  case ReaderSuccess(true, pages) =>
                    officesOfTransitReader(pages).map(_.to(TransitDomain(Some(true), _)))
                  case ReaderSuccess(false, pages) =>
                    countriesOfRoutingReader(Some(false))(pages)
                }
              case ReaderSuccess(_, pages) =>
                countriesOfRoutingReader(None)(pages)
            }
          }
    }
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}
