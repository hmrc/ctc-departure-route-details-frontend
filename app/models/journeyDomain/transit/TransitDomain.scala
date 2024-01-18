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
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess}
import pages.external.{DeclarationTypePage, OfficeOfDepartureInCL112Page, OfficeOfDeparturePage}
import pages.routing.{OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import pages.sections.Section
import pages.sections.routing.CountriesOfRoutingSection
import pages.sections.transit.TransitSection
import pages.transit.{AddOfficeOfTransitYesNoPage, T2DeclarationTypeYesNoPage}

case class TransitDomain(
  isT2DeclarationType: Option[Boolean],
  officesOfTransit: Option[OfficesOfTransitDomain]
) extends JourneyDomainModel {

  override def section: Option[Section[_]] = Some(TransitSection)
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
    ).rmap {
      case (officeOfDeparture, officeOfDepartureInCL112, officeOfDestination, officeOfDestinationInCL112) =>
        pages =>
          def countriesOfRoutingReader(isT2DeclarationType: Option[Boolean]): Read[TransitDomain] = {
            val officesOfTransit: Read[Option[OfficesOfTransitDomain]] =
              if (officeOfDepartureInCL112 || officeOfDestinationInCL112) {
                officesOfTransitReader
              } else {
                CountriesOfRoutingSection.anyCountriesOfRoutingInCL112(_).flatMap {
                  case ReaderSuccess(true, pages)  => officesOfTransitReader.apply(pages)
                  case ReaderSuccess(false, pages) => addOfficesOfTransitReader.apply(pages)
                }
              }

            officesOfTransit.map(TransitDomain(isT2DeclarationType, _))
          }

          if (officeOfDepartureInCL112 && officeOfDestinationInCL112 && officeOfDeparture.countryCode == officeOfDestination.countryCode) {
            addOfficesOfTransitReader.map(TransitDomain.apply(None, _)).apply(pages)
          } else {
            DeclarationTypePage.reader.apply(pages).flatMap {
              case ReaderSuccess(T2, pages) =>
                officesOfTransitReader.map(TransitDomain(None, _)).apply(pages)
              case ReaderSuccess(T, pages) =>
                T2DeclarationTypeYesNoPage.reader.apply(pages).flatMap {
                  case ReaderSuccess(true, pages) =>
                    officesOfTransitReader.map(TransitDomain(Some(true), _)).apply(pages)
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
