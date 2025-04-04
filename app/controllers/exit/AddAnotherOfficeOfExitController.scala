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

package controllers.exit

import config.{FrontendAppConfig, PhaseConfig}
import controllers.actions.*
import controllers.exit.index.routes as indexRoutes
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{RouteDetailsNavigatorProvider, UserAnswersNavigator}
import pages.exit.AddAnotherOfficeOfExitPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.exit.AddAnotherOfficeOfExitViewModel
import viewModels.exit.AddAnotherOfficeOfExitViewModel.AddAnotherOfficeOfExitViewModelProvider
import views.html.exit.AddAnotherOfficeOfExitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAnotherOfficeOfExitController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: RouteDetailsNavigatorProvider,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  viewModelProvider: AddAnotherOfficeOfExitViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherOfficeOfExitView
)(implicit ec: ExecutionContext, config: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherOfficeOfExitViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      viewModel.count match {
        case 0 =>
          val navigator: UserAnswersNavigator = navigatorProvider(mode)
          Redirect(navigator.nextPage(request.userAnswers, Some(AddAnotherOfficeOfExitPage)))
        case _ =>
          val preparedForm = request.userAnswers.get(AddAnotherOfficeOfExitPage) match {
            case None        => form(viewModel)
            case Some(value) => form(viewModel).fill(value)
          }
          Ok(view(preparedForm, lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, viewModel))),
          value =>
            AddAnotherOfficeOfExitPage
              .writeToUserAnswers(value)
              .updateTask()
              .writeToSession(sessionRepository)
              .and {
                if (value) {
                  _.navigateTo(indexRoutes.OfficeOfExitCountryController.onPageLoad(lrn, viewModel.nextIndex, mode))
                } else {
                  val navigator: UserAnswersNavigator = navigatorProvider(mode)
                  _.navigateWith(navigator)
                }
              }
        )
  }
}
