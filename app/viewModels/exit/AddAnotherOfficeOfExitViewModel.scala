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

package viewModels.exit

import config.FrontendAppConfig
import controllers.exit.routes
import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.exit.ExitCheckYourAnswersHelper
import viewModels.{AddAnotherViewModel, ListItem}

import javax.inject.Inject

case class AddAnotherOfficeOfExitViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "exit.addAnotherOfficeOfExit"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxOfficesOfExit
}

object AddAnotherOfficeOfExitViewModel {

  class AddAnotherOfficeOfExitViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, config: FrontendAppConfig): AddAnotherOfficeOfExitViewModel = {
      val helper = new ExitCheckYourAnswersHelper(userAnswers, mode)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherOfficeOfExitViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherOfficeOfExitController.onSubmit(userAnswers.lrn, mode)
      )
    }
  }
}
