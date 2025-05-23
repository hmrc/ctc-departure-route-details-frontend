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

package viewModels.transit

import config.FrontendAppConfig
import controllers.transit.routes
import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.transit.TransitCheckYourAnswersHelper
import viewModels.{AddAnotherViewModel, ListItem}

import javax.inject.Inject

case class AddAnotherOfficeOfTransitViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "transit.addAnotherOfficeOfTransit"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxOfficesOfTransit
}

object AddAnotherOfficeOfTransitViewModel {

  class AddAnotherOfficeOfTransitViewModelProvider @Inject() () {

    def apply(
      userAnswers: UserAnswers,
      mode: Mode
    )(implicit messages: Messages, config: FrontendAppConfig): AddAnotherOfficeOfTransitViewModel = {
      val helper = new TransitCheckYourAnswersHelper(userAnswers, mode)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherOfficeOfTransitViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherOfficeOfTransitController.onSubmit(userAnswers.lrn, mode)
      )
    }
  }
}
