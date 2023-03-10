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
import models.SecurityDetailsType.{EntryAndExitSummaryDeclarationSecurityDetails, EntrySummaryDeclarationSecurityDetails, ExitSummaryDeclarationSecurityDetails}
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Mode, UserAnswers}
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

  // When pre-lodge is in, add a loadingReader to handle nav logic if additional declaration type is A or D

  implicit val unloadingReader: UserAnswersReader[Option[UnloadingDomain]] =
    SecurityDetailsTypePage.reader.flatMap {
      case ExitSummaryDeclarationSecurityDetails =>
        AddPlaceOfUnloadingPage
          .filterOptionalDependent(identity)(UserAnswersReader[UnloadingDomain])
      case EntrySummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails =>
        UserAnswersReader[UnloadingDomain].map(Some(_))
      case _ =>
        none[UnloadingDomain].pure[UserAnswersReader]
    }

  implicit val userAnswersReader: UserAnswersReader[LoadingAndUnloadingDomain] =
    (
      UserAnswersReader[LoadingDomain].map(Some(_)),
      UserAnswersReader[Option[UnloadingDomain]]
    ).tupled.map((LoadingAndUnloadingDomain.apply _).tupled)
}
