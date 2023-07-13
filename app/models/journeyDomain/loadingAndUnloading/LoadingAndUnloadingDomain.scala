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
import config.PhaseConfig
import models.SecurityDetailsType.{
  EntryAndExitSummaryDeclarationSecurityDetails,
  EntrySummaryDeclarationSecurityDetails,
  ExitSummaryDeclarationSecurityDetails,
  NoSecurityDetails
}
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Mode, Phase, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.loadingAndUnloading.AddPlaceOfUnloadingPage
import play.api.mvc.Call

case class LoadingAndUnloadingDomain(
  loading: Option[LoadingDomain],
  unloading: Option[UnloadingDomain]
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(controllers.loadingAndUnloading.routes.LoadingAndUnloadingAnswersController.onPageLoad(userAnswers.lrn, mode))
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
        // additional declaration type is currently always normal (A) as we aren't doing pre-lodge (D) yet
        UserAnswersReader[LoadingDomain].map(Some(_))
    }

  implicit def unloadingReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[UnloadingDomain]] = {
    lazy val mandatoryReader: UserAnswersReader[Option[UnloadingDomain]] = UserAnswersReader[UnloadingDomain].map(Some(_))
    phaseConfig.phase match {
      case Phase.Transition =>
        // specific circumstance indicator is currently undefined, so draw.io condition is always false
        mandatoryReader
      case Phase.PostTransition =>
        SecurityDetailsTypePage.reader.flatMap {
          case ExitSummaryDeclarationSecurityDetails =>
            AddPlaceOfUnloadingPage.filterOptionalDependent(identity)(UserAnswersReader[UnloadingDomain])
          case EntrySummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails =>
            mandatoryReader
          case _ =>
            none[UnloadingDomain].pure[UserAnswersReader]
        }
    }
  }

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[LoadingAndUnloadingDomain] =
    (
      UserAnswersReader[Option[LoadingDomain]],
      UserAnswersReader[Option[UnloadingDomain]]
    ).tupled.map((LoadingAndUnloadingDomain.apply _).tupled)
}
