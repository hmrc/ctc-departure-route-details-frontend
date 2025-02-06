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

package controllers.loadingAndUnloading.unloading

import config.FrontendAppConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.UnLocodeFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{LoadingAndUnloadingNavigatorProvider, UserAnswersNavigator}
import pages.loadingAndUnloading.unloading.UnLocodePage
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UnLocodesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.loadingAndUnloading.unloading.UnLocodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnLocodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: LoadingAndUnloadingNavigatorProvider,
  actions: Actions,
  formProvider: UnLocodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  unLocodesService: UnLocodesService,
  view: UnLocodeView
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "loadingAndUnloading.unloading.unLocode"
  private val form           = formProvider(prefix)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val preparedForm = request.userAnswers.get(UnLocodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, lrn, mode))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode))),
          value =>
            unLocodesService.doesUnLocodeExist(value).flatMap {
              case true =>
                val navigator: UserAnswersNavigator = navigatorProvider(mode)
                UnLocodePage
                  .writeToUserAnswers(value)
                  .updateTask()
                  .writeToSession(sessionRepository)
                  .navigateWith(navigator)
              case false =>
                val formWithErrors = form.withError(FormError("value", s"$prefix.error.not.exists"))
                Future.successful(BadRequest(view(formWithErrors, lrn, mode)))
            }
        )
  }
}
