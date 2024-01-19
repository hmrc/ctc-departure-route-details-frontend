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

package controllers.exit.index

import config.FrontendAppConfig
import controllers.actions._
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{ExitNavigatorProvider, UserAnswersNavigator}
import pages.sections.exit.OfficeOfExitSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.exit.OfficeOfExitAnswersViewModel.OfficeOfExitAnswersViewModelProvider
import views.html.exit.index.CheckOfficeOfExitAnswersView

import javax.inject.Inject

class CheckOfficeOfExitAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: ExitNavigatorProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: CheckOfficeOfExitAnswersView,
  viewModelProvider: OfficeOfExitAnswersViewModelProvider
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val section = viewModelProvider(request.userAnswers, mode, index).section
      Ok(view(lrn, index, mode, Seq(section)))
  }

  def onSubmit(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val navigator: UserAnswersNavigator = navigatorProvider(mode)
      Redirect(navigator.nextPage(request.userAnswers, Some(OfficeOfExitSection(index))))
  }
}
