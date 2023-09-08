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

package models.journeyDomain

import config.Constants._
import cats.implicits._
import config.PhaseConfig
import models.DeclarationType.Option4
import models.SecurityDetailsType._
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.exit.ExitDomain
import models.journeyDomain.loadingAndUnloading.LoadingAndUnloadingDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.routing.RoutingDomain
import models.journeyDomain.transit.TransitDomain
import models.reference.SpecificCircumstanceIndicator
import models.{DeclarationType, Mode, Phase, SecurityDetailsType, UserAnswers}
import pages.external.{AdditionalDeclarationTypePage, DeclarationTypePage, OfficeOfDepartureInCL147Page, SecurityDetailsTypePage}
import pages.locationOfGoods.AddLocationOfGoodsPage
import pages.sections.routing.CountriesOfRoutingSection
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}
import play.api.mvc.Call

case class RouteDetailsDomain(
  specificCircumstanceIndicator: Option[SpecificCircumstanceIndicator],
  routing: RoutingDomain,
  transit: Option[TransitDomain],
  exit: Option[ExitDomain],
  locationOfGoods: Option[LocationOfGoodsDomain],
  loadingAndUnloading: LoadingAndUnloadingDomain
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(controllers.routes.RouteDetailsAnswersController.onPageLoad(userAnswers.lrn))
}

object RouteDetailsDomain {

  implicit val specificCircumstanceIndicatorReader: UserAnswersReader[Option[SpecificCircumstanceIndicator]] =
    SecurityDetailsTypePage.reader.flatMap {
      case ExitSummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails =>
        AddSpecificCircumstanceIndicatorYesNoPage.filterOptionalDependent(identity)(SpecificCircumstanceIndicatorPage.reader)
      case _ => none[SpecificCircumstanceIndicator].pure[UserAnswersReader]
    }

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[RouteDetailsDomain] =
    for {
      specificCircumstanceIndicator <- UserAnswersReader[Option[SpecificCircumstanceIndicator]]
      routing                       <- UserAnswersReader[RoutingDomain]
      transit                       <- UserAnswersReader[Option[TransitDomain]]
      exit                          <- UserAnswersReader[Option[ExitDomain]](exitReader(transit))
      locationOfGoods               <- UserAnswersReader[Option[LocationOfGoodsDomain]]
      loadingAndUnloading           <- UserAnswersReader[LoadingAndUnloadingDomain]
    } yield RouteDetailsDomain(
      specificCircumstanceIndicator,
      routing,
      transit,
      exit,
      locationOfGoods,
      loadingAndUnloading
    )

  implicit def transitReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[TransitDomain]] =
    DeclarationTypePage.reader.flatMap {
      case Option4 => none[TransitDomain].pure[UserAnswersReader]
      case _       => UserAnswersReader[TransitDomain].map(Some(_))
    }

  implicit def exitReader(transit: Option[TransitDomain]): UserAnswersReader[Option[ExitDomain]] =
    for {
      declarationType                      <- DeclarationTypePage.reader
      securityDetails                      <- SecurityDetailsTypePage.reader
      atLeastOneCountryOfRoutingNotInCL147 <- CountriesOfRoutingSection.atLeastOneCountryOfRoutingNotInCL147
      reader <- {
        if (exitRequired(declarationType, securityDetails, atLeastOneCountryOfRoutingNotInCL147, transit)) {
          UserAnswersReader[ExitDomain].map(Some(_))
        } else {
          none[ExitDomain].pure[UserAnswersReader]
        }
      }
    } yield reader

  private def exitRequired(
    declarationType: DeclarationType,
    securityDetails: SecurityDetailsType,
    atLeastOneCountryOfRoutingNotInCL147: Boolean,
    transit: Option[TransitDomain]
  ): Boolean =
    (declarationType, securityDetails, atLeastOneCountryOfRoutingNotInCL147, transit) match {
      case (Option4, _, _, _)                                                    => false
      case (_, NoSecurityDetails | EntrySummaryDeclarationSecurityDetails, _, _) => false
      case (_, _, true, Some(TransitDomain(_, _ :: _)))                          => false
      case _                                                                     => true
    }

  implicit def locationOfGoodsReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[LocationOfGoodsDomain]] = {
    lazy val optionalReader = AddLocationOfGoodsPage.filterOptionalDependent(identity)(UserAnswersReader[LocationOfGoodsDomain])
    phaseConfig.phase match {
      case Phase.Transition =>
        AdditionalDeclarationTypePage.reader.flatMap {
          case `PRE-LODGE` =>
            UserAnswersReader[LocationOfGoodsDomain].map(Some(_))
          case _ =>
            optionalReader
        }
      case Phase.PostTransition =>
        AdditionalDeclarationTypePage.reader.flatMap {
          case `PRE-LODGE` =>
            optionalReader
          case _ =>
            OfficeOfDepartureInCL147Page.reader.flatMap {
              case true  => optionalReader
              case false => UserAnswersReader[LocationOfGoodsDomain].map(Some(_))
            }
        }
    }
  }
}
