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

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.CustomsOfficeFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{LocationOfGoodsNavigatorProvider, UserAnswersNavigator}
import pages.external.OfficeOfDeparturePage
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CustomsOfficesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.CustomsOfficeIdentifierView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficeIdentifierController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: LocationOfGoodsNavigatorProvider,
  actions: Actions,
  formProvider: CustomsOfficeFormProvider,
  customsOfficesService: CustomsOfficesService,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CustomsOfficeIdentifierView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(OfficeOfDeparturePage))
    .async {
      implicit request =>
        val office = request.arg
        customsOfficesService.getCustomsOfficesOfDepartureForCountry(office.countryCode).map {
          customsOfficeList =>
            val form = formProvider("locationOfGoods.customsOfficeIdentifier", customsOfficeList)
            val preparedForm = request.userAnswers.get(CustomsOfficeIdentifierPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, lrn, customsOfficeList.customsOffices, mode))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(OfficeOfDeparturePage))
    .async {
      implicit request =>
        val office = request.arg
        customsOfficesService.getCustomsOfficesOfDepartureForCountry(office.countryCode).flatMap {
          customsOfficeList =>
            val form = formProvider("locationOfGoods.customsOfficeIdentifier", customsOfficeList)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, customsOfficeList.customsOffices, mode))),
                value => {
                  implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                  CustomsOfficeIdentifierPage
                    .writeToUserAnswers(value)
                    .updateTask()
                    .writeToSession()
                    .navigate()
                }
              )
        }
    }
}
