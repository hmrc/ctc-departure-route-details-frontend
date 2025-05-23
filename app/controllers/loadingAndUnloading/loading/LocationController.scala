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

package controllers.loadingAndUnloading.loading

import controllers.actions.*
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.Constants.loadingLocationMaxLength
import forms.LoadingLocationFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{LoadingAndUnloadingNavigatorProvider, UserAnswersNavigator}
import pages.loadingAndUnloading.loading.{CountryPage, LocationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.loadingAndUnloading.loading.LocationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: LoadingAndUnloadingNavigatorProvider,
  formProvider: LoadingLocationFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: LocationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(CountryPage)) {
      implicit request =>
        val countryName = request.arg.description
        val form        = formProvider("loadingAndUnloading.loading.location", countryName)
        val preparedForm = request.userAnswers.get(LocationPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, lrn, countryName, loadingLocationMaxLength, mode))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(CountryPage))
    .async {
      implicit request =>
        val countryName = request.arg.description
        val form        = formProvider("loadingAndUnloading.loading.location", countryName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, countryName, loadingLocationMaxLength, mode))),
            value => {
              val navigator: UserAnswersNavigator = navigatorProvider(mode)
              LocationPage
                .writeToUserAnswers(value)
                .updateTask()
                .writeToSession(sessionRepository)
                .navigateWith(navigator)
            }
          )
    }
}
