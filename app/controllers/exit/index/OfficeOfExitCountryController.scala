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

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.CountryFormProvider
import models.reference.Country
import models.requests.SpecificDataRequestProvider1
import models.{CountryList, Index, LocalReferenceNumber, Mode}
import navigation.{OfficeOfExitNavigatorProvider, UserAnswersNavigator}
import pages.QuestionPage
import pages.exit.index.{InferredOfficeOfExitCountryPage, OfficeOfExitCountryPage}
import pages.routing.CountryOfDestinationPage
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{CountriesService, CustomsOfficesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.exit.index.OfficeOfExitCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeOfExitCountryController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: OfficeOfExitNavigatorProvider,
  actions: Actions,
  formProvider: CountryFormProvider,
  countriesService: CountriesService,
  customsOfficesService: CustomsOfficesService,
  val controllerComponents: MessagesControllerComponents,
  view: OfficeOfExitCountryView,
  getMandatoryPage: SpecificDataRequiredActionProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "exit.index.officeOfExitCountry"

  private type Request = SpecificDataRequestProvider1[Country]#SpecificDataRequest[_]

  def onPageLoad(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(CountryOfDestinationPage))
    .async {
      implicit request =>
        countriesService.getOfficeOfExitCountries(request.userAnswers, request.arg).flatMap {
          case CountryList(country :: Nil) =>
            redirect(mode, index, InferredOfficeOfExitCountryPage, country)
          case countryList =>
            val form = formProvider(prefix, countryList)
            val preparedForm = request.userAnswers.get(OfficeOfExitCountryPage(index)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Future.successful(Ok(view(preparedForm, lrn, countryList.countries, index, mode)))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(CountryOfDestinationPage))
    .async {
      implicit request =>
        countriesService.getOfficeOfExitCountries(request.userAnswers, request.arg).flatMap {
          countryList =>
            val form = formProvider(prefix, countryList)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, countryList.countries, index, mode))),
                value =>
                  customsOfficesService.getCustomsOfficesOfExitForCountry(value.code).flatMap {
                    case x if x.customsOffices.nonEmpty =>
                      redirect(mode, index, OfficeOfExitCountryPage, value)
                    case _ =>
                      val formWithErrors = form.withError(FormError("value", s"$prefix.error.noOffices"))
                      Future.successful(BadRequest(view(formWithErrors, lrn, countryList.countries, index, mode)))
                  }
              )
        }
    }

  private def redirect(
    mode: Mode,
    index: Index,
    page: Index => QuestionPage[Country],
    country: Country
  )(implicit request: Request): Future[Result] =
    for {
      ctcCountries                          <- countriesService.getCountryCodesCTC()
      customsSecurityAgreementAreaCountries <- countriesService.getCustomsSecurityAgreementAreaCountries()
      result <- {
        implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index, ctcCountries, customsSecurityAgreementAreaCountries)
        page(index)
          .writeToUserAnswers(country)
          .updateTask(ctcCountries, customsSecurityAgreementAreaCountries)
          .writeToSession()
          .navigate()
      }
    } yield result
}
