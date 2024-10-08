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

import config.FrontendAppConfig
import controllers.actions._
import controllers.exit.index.{routes => indexRoutes}
import forms.AddAnotherFormProvider
import models.requests.DataRequest
import models.{LocalReferenceNumber, Mode}
import navigation.{RouteDetailsNavigatorProvider, UserAnswersNavigator}
import pages.sections.exit.OfficesOfExitSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.exit.AddAnotherOfficeOfExitViewModel
import viewModels.exit.AddAnotherOfficeOfExitViewModel.AddAnotherOfficeOfExitViewModelProvider
import views.html.exit.AddAnotherOfficeOfExitView

import javax.inject.Inject

class AddAnotherOfficeOfExitController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: RouteDetailsNavigatorProvider,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  viewModelProvider: AddAnotherOfficeOfExitViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherOfficeOfExitView
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherOfficeOfExitViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      viewModel.count match {
        case 0 => redirectToNextPage(mode)
        case _ => Ok(view(form(viewModel), lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, lrn, viewModel)),
          {
            case true  => Redirect(indexRoutes.OfficeOfExitCountryController.onPageLoad(lrn, viewModel.nextIndex, mode))
            case false => redirectToNextPage(mode)
          }
        )
  }

  private def redirectToNextPage(mode: Mode)(implicit request: DataRequest[?]): Result = {
    val navigator: UserAnswersNavigator = navigatorProvider(mode)
    Redirect(navigator.nextPage(request.userAnswers, Some(OfficesOfExitSection)))
  }
}
