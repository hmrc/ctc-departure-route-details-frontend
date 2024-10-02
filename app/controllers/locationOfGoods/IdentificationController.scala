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

package controllers.locationOfGoods

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EnumerableFormProvider
import models.reference.{LocationOfGoodsIdentification, LocationType}
import models.requests.SpecificDataRequestProvider1
import models.{LocalReferenceNumber, Mode}
import navigation.{LocationOfGoodsNavigatorProvider, UserAnswersNavigator}
import pages.QuestionPage
import pages.locationOfGoods.{IdentificationPage, InferredIdentificationPage, InferredLocationTypePage, LocationTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.LocationOfGoodsIdentificationTypeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.IdentificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: LocationOfGoodsNavigatorProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: EnumerableFormProvider,
  locationOfGoodsIdentificationTypeService: LocationOfGoodsIdentificationTypeService,
  val controllerComponents: MessagesControllerComponents,
  view: IdentificationView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private type Request = SpecificDataRequestProvider1[LocationType]#SpecificDataRequest[?]

  private def form(locationOfGoodsIdentification: Seq[LocationOfGoodsIdentification]): Form[LocationOfGoodsIdentification] =
    formProvider("locationOfGoods.locationOfGoodsIdentificationType", locationOfGoodsIdentification)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(LocationTypePage, InferredLocationTypePage))
    .async {
      implicit request =>
        locationOfGoodsIdentificationTypeService.getLocationOfGoodsIdentificationTypes(request.arg).flatMap {
          case identifier :: Nil =>
            redirect(mode, InferredIdentificationPage, identifier)
          case identifiers =>
            val preparedForm = request.userAnswers.get(IdentificationPage) match {
              case None        => form(identifiers)
              case Some(value) => form(identifiers).fill(value)
            }

            Future.successful(Ok(view(preparedForm, lrn, identifiers, mode)))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(LocationTypePage, InferredLocationTypePage))
    .async {
      implicit request =>
        locationOfGoodsIdentificationTypeService.getLocationOfGoodsIdentificationTypes(request.arg).flatMap {
          locationOfGoodsIdentificationTypes =>
            form(locationOfGoodsIdentificationTypes)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, locationOfGoodsIdentificationTypes, mode))),
                value => redirect(mode, IdentificationPage, value)
              )
        }
    }

  private def redirect(
    mode: Mode,
    page: QuestionPage[LocationOfGoodsIdentification],
    value: LocationOfGoodsIdentification
  )(implicit request: Request): Future[Result] = {
    val navigator: UserAnswersNavigator = navigatorProvider(mode)
    page
      .writeToUserAnswers(value)
      .updateTask()
      .writeToSession(sessionRepository)
      .navigateWith(navigator)
  }
}
