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
import config.Constants.CountryCode._
import config.Constants.SecurityType._
import config.PhaseConfig
import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess, Stage}
import models.reference.{Country, CustomsOffice}
import models.{DateTime, Index, Mode, Phase, UserAnswers}
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
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
  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): Read[OfficeOfTransitDomain] = {

    lazy val etaReads: Read[Option[DateTime]] = {
      phaseConfig.phase match {
        case Phase.PostTransition =>
          SecurityDetailsTypePage.reader.apply(_).flatMap {
            case ReaderSuccess(EntrySummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails, pages) =>
              OfficeOfTransitInCL147Page(index).reader.apply(pages).flatMap {
                case ReaderSuccess(true, pages) =>
                  OfficeOfTransitETAPage(index).reader.apply(pages).map(_.toOption)
                case ReaderSuccess(false, pages) =>
                  AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader).apply(pages)
              }
            case ReaderSuccess(_, pages) =>
              AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader).apply(pages)
          }

        case Phase.Transition =>
          SecurityDetailsTypePage.reader.apply(_).flatMap {
            case ReaderSuccess(NoSecurityDetails, pages) =>
              AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader).apply(pages)
            case ReaderSuccess(_, pages) =>
              (
                OfficeOfTransitInCL010Page(index).reader,
                OfficeOfDepartureInCL010Page.reader
              ).tupleIt[Option[DateTime]] {
                case (true, false) => OfficeOfTransitETAPage(index).reader.apply(_).map(_.toOption)
                case _             => AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader)
              }.apply(pages)
          }
      }
    }

    lazy val readsWithoutCountry: Read[OfficeOfTransitDomain] =
      (
        OfficeOfTransitPage(index).reader,
        etaReads
      ).mapReads(OfficeOfTransitDomain.apply(None, _, _)(index))

    lazy val readsWithCountry: Read[OfficeOfTransitDomain] =
      (
        UserAnswersReader.readInferred(OfficeOfTransitCountryPage(index), InferredOfficeOfTransitCountryPage(index)).map(_.toOption),
        OfficeOfTransitPage(index).reader,
        etaReads
      ).mapReads(OfficeOfTransitDomain.apply(_, _, _)(index))

    (index.position, phaseConfig.phase) match {
      case (0, Phase.PostTransition) =>
        OfficeOfDestinationInCL112Page.reader.apply(_).flatMap {
          case ReaderSuccess(true, pages) =>
            readsWithoutCountry(pages)
          case ReaderSuccess(false, pages) =>
            OfficeOfDestinationPage.reader.apply(pages).map(_.to(_.countryCode)).flatMap {
              case ReaderSuccess(AD, pages) => readsWithoutCountry(pages)
              case ReaderSuccess(_, pages)  => readsWithCountry(pages)
            }
        }
      case (_, Phase.Transition) =>
        OfficeOfDestinationPage.reader.apply(_).map(_.to(_.countryCode)).flatMap {
          case ReaderSuccess(AD, pages) => readsWithoutCountry(pages)
          case ReaderSuccess(_, pages)  => readsWithCountry(pages)
        }
      case _ =>
        readsWithCountry(_)
    }
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}
