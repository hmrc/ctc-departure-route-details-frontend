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

package navigation

import config.{FrontendAppConfig, PhaseConfig}
import models.domain.UserAnswersReader
import models.journeyDomain.OpsError.ReaderError
import models.journeyDomain.Stage.CompletingJourney
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Mode, UserAnswers}
import play.api.Logging
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs.GET

trait UserAnswersNavigator extends Navigator {

  implicit val config: FrontendAppConfig
  implicit val phaseConfig: PhaseConfig

  type T <: JourneyDomainModel

  implicit val reader: UserAnswersReader[T]

  val mode: Mode

  override def nextPage(userAnswers: UserAnswers): Call =
    UserAnswersNavigator.nextPage[T](userAnswers, mode)
}

object UserAnswersNavigator extends Logging {

  def nextPage[T <: JourneyDomainModel](
    userAnswers: UserAnswers,
    mode: Mode,
    stage: Stage = CompletingJourney
  )(implicit userAnswersReader: UserAnswersReader[T], config: FrontendAppConfig): Call = {
    lazy val errorCall = Call(GET, config.notFoundUrl)

    userAnswersReader.run(userAnswers) match {
      case Left(ReaderError(page, _)) =>
        page.route(userAnswers, mode).getOrElse {
          logger.error(s"Route not defined for page ${page.path}") // TODO reduce severity
          errorCall
        }
      case Right(x) =>
        x.routeIfCompleted(userAnswers, mode, stage).getOrElse {
          logger.error(s"Completed route not defined for model $x") // TODO reduce severity
          errorCall
        }
    }
  }
}
