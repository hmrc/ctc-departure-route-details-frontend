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

package models.journeyDomain.loadingAndUnloading

import cats.implicits._
import config.Constants._
import config.{Constants, PhaseConfig}
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.SpecificCircumstanceIndicator
import models.{Mode, Phase, UserAnswers}
import pages.SpecificCircumstanceIndicatorPage
import pages.external.{AdditionalDeclarationTypePage, SecurityDetailsTypePage}
import pages.loadingAndUnloading.{AddPlaceOfLoadingYesNoPage, AddPlaceOfUnloadingPage}
import play.api.mvc.Call

case class LoadingAndUnloadingDomain(
  loading: Option[LoadingDomain],
  unloading: Option[UnloadingDomain]
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    if (loading.isEmpty && unloading.isEmpty) {
      Some(controllers.routes.RouteDetailsAnswersController.onPageLoad(userAnswers.lrn))
    } else {
      Some(controllers.loadingAndUnloading.routes.LoadingAndUnloadingAnswersController.onPageLoad(userAnswers.lrn, mode))
    }
}

object LoadingAndUnloadingDomain {

  implicit def loadingReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[LoadingDomain]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        SecurityDetailsTypePage.reader.flatMap {
          case NoSecurityDetails => none[LoadingDomain].pure[UserAnswersReader]
          case _                 => UserAnswersReader[LoadingDomain].map(Some(_))
        }
      case Phase.PostTransition =>
        AdditionalDeclarationTypePage.reader.flatMap {
          case Constants.`PRE-LODGE` => AddPlaceOfLoadingYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[LoadingDomain])
          case _                     => UserAnswersReader[LoadingDomain].map(Some(_))
        }
    }

  implicit def unloadingReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[UnloadingDomain]] = {
    lazy val mandatoryReader: UserAnswersReader[Option[UnloadingDomain]] =
      UserAnswersReader[UnloadingDomain].map(Some(_))

    lazy val optionalReader: UserAnswersReader[Option[UnloadingDomain]] =
      AddPlaceOfUnloadingPage.filterOptionalDependent(identity)(UserAnswersReader[UnloadingDomain])

    phaseConfig.phase match {
      case Phase.Transition =>
        SecurityDetailsTypePage.reader.flatMap {
          case NoSecurityDetails => none[UnloadingDomain].pure[UserAnswersReader]
          case _ =>
            SpecificCircumstanceIndicatorPage.optionalReader.flatMap {
              case Some(SpecificCircumstanceIndicator(XXX, _)) => optionalReader
              case _                                           => mandatoryReader
            }
        }
      case Phase.PostTransition =>
        SecurityDetailsTypePage.reader.flatMap {
          case NoSecurityDetails                     => none[UnloadingDomain].pure[UserAnswersReader]
          case ExitSummaryDeclarationSecurityDetails => optionalReader
          case _                                     => mandatoryReader
        }
    }
  }

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[LoadingAndUnloadingDomain] =
    (
      UserAnswersReader[Option[LoadingDomain]],
      UserAnswersReader[Option[UnloadingDomain]]
    ).tupled.map((LoadingAndUnloadingDomain.apply _).tupled)
}
