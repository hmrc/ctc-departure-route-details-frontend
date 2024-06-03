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

import config.PhaseConfig
import controllers.actions._
import controllers.exit.{routes => exitRoutes}
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import pages.sections.exit.OfficeOfExitSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.exit.RemoveOfficeOfExitViewModel
import viewModels.exit.RemoveOfficeOfExitViewModel.RemoveOfficeOfExitViewModelProvider
import views.html.exit.index.ConfirmRemoveOfficeOfExitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveOfficeOfExitController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmRemoveOfficeOfExitView,
  viewModelProvider: RemoveOfficeOfExitViewModelProvider
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: RemoveOfficeOfExitViewModel): Form[Boolean] =
    formProvider(viewModel.prefix)

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    exitRoutes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode)

  def onPageLoad(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(lrn, OfficeOfExitSection(index), addAnother(lrn, mode)) {
      implicit request =>
        val viewModel = viewModelProvider.apply(request.userAnswers, index)
        Ok(view(form(viewModel), lrn, index, mode, viewModel, viewModel.officeName))
    }

  def onSubmit(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions
    .requireIndex(lrn, OfficeOfExitSection(index), addAnother(lrn, mode))
    .async {
      implicit request =>
        val viewModel = viewModelProvider.apply(request.userAnswers, index)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, index, mode, viewModel, viewModel.officeName))),
            {
              case true =>
                OfficeOfExitSection(index)
                  .removeFromUserAnswers()
                  .updateTask()
                  .writeToSession()
                  .navigateTo(addAnother(lrn, mode))
              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }
}
