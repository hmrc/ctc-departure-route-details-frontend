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

package models.journeyDomain.exit

import models.domain.{JsArrayGettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Index, Mode, RichJsArray, UserAnswers}
import pages.sections.exit.OfficesOfExitSection
import play.api.mvc.Call

case class ExitDomain(
  officesOfExit: Seq[OfficeOfExitDomain]
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(controllers.exit.routes.AddAnotherOfficeOfExitController.onPageLoad(userAnswers.lrn, mode))
}

object ExitDomain {

  implicit val userAnswersReader: UserAnswersReader[ExitDomain] = {

    implicit val officesOfExitReader: UserAnswersReader[Seq[OfficeOfExitDomain]] =
      OfficesOfExitSection.arrayReader.flatMap {
        case x if x.isEmpty =>
          UserAnswersReader[OfficeOfExitDomain](
            OfficeOfExitDomain.userAnswersReader(Index(0))
          ).map(Seq(_))
        case x =>
          x.traverse[OfficeOfExitDomain](
            OfficeOfExitDomain.userAnswersReader
          )
      }

    UserAnswersReader[Seq[OfficeOfExitDomain]].map(ExitDomain(_))
  }
}
