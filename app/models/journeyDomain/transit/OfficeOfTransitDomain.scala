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

import cats.implicits._
import config.Constants.AD
import config.PhaseConfig
import models.SecurityDetailsType.{EntryAndExitSummaryDeclarationSecurityDetails, EntrySummaryDeclarationSecurityDetails}
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.{Country, CustomsOffice}
import models.{DateTime, Index, Mode, Phase, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.routing.{OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import pages.transit.index._
import play.api.mvc.Call

case class OfficeOfTransitDomain(
  country: Option[Country],
  customsOffice: CustomsOffice,
  eta: Option[DateTime]
)(index: Index)
    extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(
      controllers.transit.index.routes.CheckOfficeOfTransitAnswersController.onPageLoad(userAnswers.lrn, mode, index)
    )

  val label: String = country match {
    case Some(value) => s"$value - $customsOffice"
    case None        => customsOffice.toString
  }

}

object OfficeOfTransitDomain {

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): UserAnswersReader[OfficeOfTransitDomain] = {

    lazy val etaReads: UserAnswersReader[Option[DateTime]] =
      SecurityDetailsTypePage.reader.flatMap {
        case EntrySummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails =>
          OfficeOfTransitInCL147Page(index).reader.flatMap {
            case true =>
              OfficeOfTransitETAPage(index).reader.map(Some(_))
            case false =>
              AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader)
          }
        case _ =>
          AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader)
      }

    lazy val readsWithoutCountry: UserAnswersReader[OfficeOfTransitDomain] =
      (
        OfficeOfTransitPage(index).reader,
        etaReads
      ).mapN {
        (office, eta) => OfficeOfTransitDomain(None, office, eta)(index)
      }

    lazy val readsWithCountry: UserAnswersReader[OfficeOfTransitDomain] =
      (
        InferredOfficeOfTransitCountryPage(index).reader orElse OfficeOfTransitCountryPage(index).reader,
        OfficeOfTransitPage(index).reader,
        etaReads
      ).mapN {
        (country, office, eta) => OfficeOfTransitDomain(Some(country), office, eta)(index)
      }

    (index.position, phaseConfig.phase) match {
      case (0, Phase.PostTransition) =>
        OfficeOfDestinationInCL112Page.reader.flatMap {
          case true =>
            readsWithoutCountry
          case false =>
            OfficeOfDestinationPage.reader.map(_.countryCode).flatMap {
              case AD => readsWithoutCountry
              case _  => readsWithCountry
            }
        }
      case (_, Phase.Transition) =>
        OfficeOfDestinationPage.reader.map(_.countryCode).flatMap {
          case AD => readsWithoutCountry
          case _  => readsWithCountry
        }
      case _ => readsWithCountry
    }
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}
