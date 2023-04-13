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

import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, JsArrayGettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.transit.TransitDomain.OfficesOfTransit
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{DeclarationType, Index, Mode, RichJsArray, UserAnswers}
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
  implicit val userAnswersReader: UserAnswersReader[TransitDomain] = {

    implicit val officesOfTransitReader: UserAnswersReader[OfficesOfTransit] =
      OfficesOfTransitSection.arrayReader.flatMap {
        case x if x.isEmpty =>
          UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(Index(0))
          ).map(Seq(_))
        case x =>
          x.traverse[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader
          )
      }

    lazy val addOfficesOfTransitReader: UserAnswersReader[OfficesOfTransit] =
      AddOfficeOfTransitYesNoPage
        .filterOptionalDependent(identity)(officesOfTransitReader)
        .map(_.getOrElse(Nil))

    for {
      officeOfDeparture          <- OfficeOfDeparturePage.reader
      officeOfDepartureInCL112   <- OfficeOfDepartureInCL112Page.reader
      officeOfDestination        <- OfficeOfDestinationPage.reader
      officeOfDestinationInCL112 <- OfficeOfDestinationInCL112Page.reader
      reader <- {
        def countriesOfRoutingReader(isT2DeclarationType: Option[Boolean]): UserAnswersReader[TransitDomain] = {
          val officesOfTransit = if (officeOfDepartureInCL112 || officeOfDestinationInCL112) {
            UserAnswersReader[OfficesOfTransit]
          } else {
            for {
              anyCountriesOfRoutingInCL112 <- CountriesOfRoutingSection.anyCountriesOfRoutingInCL112
              reader <-
                if (anyCountriesOfRoutingInCL112) {
                  UserAnswersReader[OfficesOfTransit]
                } else {
                  addOfficesOfTransitReader
                }
            } yield reader
          }

          officesOfTransit.map(TransitDomain(isT2DeclarationType, _))
        }

        if (officeOfDepartureInCL112 && officeOfDestinationInCL112 && officeOfDeparture.countryCode == officeOfDestination.countryCode) {
          addOfficesOfTransitReader.map(TransitDomain(None, _))
        } else {
          DeclarationTypePage.reader.flatMap {
            case DeclarationType.Option2 =>
              UserAnswersReader[OfficesOfTransit].map(TransitDomain(None, _))
            case DeclarationType.Option5 =>
              T2DeclarationTypeYesNoPage.reader.flatMap {
                case true =>
                  UserAnswersReader[OfficesOfTransit].map(TransitDomain(Some(true), _))
                case false =>
                  countriesOfRoutingReader(Some(false))
              }
            case _ =>
              countriesOfRoutingReader(None)
          }
        }
      }
    } yield reader
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}
