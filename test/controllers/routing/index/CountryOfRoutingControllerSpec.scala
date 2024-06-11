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

package controllers.routing.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider
import generators.Generators
import models.reference.{Country, CountryCode, CustomsOffice}
import models.{Index, NormalMode, SelectableList, UserAnswers}
import navigation.CountryOfRoutingNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.routing.index.CountryOfRoutingPage
import pages.transit.index.OfficeOfTransitPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.routing.index.CountryOfRoutingView

import scala.concurrent.Future

class CountryOfRoutingControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val country1      = arbitraryCountry.arbitrary.sample.get
  private val country2      = arbitraryCountry.arbitrary.sample.get
  private val countryFrance = Country(CountryCode("FR"), "France")
  private val countryList   = SelectableList(Seq(country1, country2, countryFrance))

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("routing.index.countryOfRouting", countryList)
  private val mode         = NormalMode

  private lazy val countryOfRoutingRoute = routes.CountryOfRoutingController.onPageLoad(lrn, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[CountryOfRoutingNavigatorProvider]).toInstance(fakeCountryOfRoutingNavigatorProvider))

  "CountryOfRouting Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCountriesService.getFilteredCountriesOfRouting(any(), any())(any())).thenReturn(Future.successful(countryList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, countryOfRoutingRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CountryOfRoutingView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, countryList.values, mode, index)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      val userAnswers = emptyUserAnswers.setValue(CountryOfRoutingPage(index), country1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, countryOfRoutingRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> country1.code.code))

      val view = injector.instanceOf[CountryOfRoutingView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, countryList.values, mode, index)(request, messages).toString
    }
    "must redirect to the next page when valid data is submitted" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      when(mockCountriesService.getCountryCodesCTC()(any())).thenReturn(Future.successful(countryList))
      when(mockCountriesService.getCustomsSecurityAgreementAreaCountries()(any())).thenReturn(Future.successful(countryList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, countryOfRoutingRoute)
        .withFormUrlEncodedBody(("value", country1.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.data mustBe Json.parse(s"""
          |{
          |  "routeDetails" : {
          |    "routing" : {
          |      "countriesOfRouting" : [
          |        {
          |          "countryOfRouting" : {
          |            "code" : "${country1.code.code}",
          |            "description" : "${country1.description}",
          |            "isInCL112" : true,
          |            "isInCL147" : true
          |          }
          |        }
          |      ]
          |    }
          |  }
          |}
          |""".stripMargin)
    }

    "must redirect to add another country of routing  and remove officeOfTransits with the changed country code" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      when(mockCountriesService.getCountryCodesCTC()(any())).thenReturn(Future.successful(countryList))
      when(mockCountriesService.getCustomsSecurityAgreementAreaCountries()(any())).thenReturn(Future.successful(countryList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(CountryOfRoutingPage(index), country1)
        .setValue(OfficeOfTransitPage(index), CustomsOffice(country1.code.code, "port1", None, country1.code.code))
        .setValue(OfficeOfTransitPage(Index(1)), CustomsOffice(country1.code.code, "port2", None, country1.code.code))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, countryOfRoutingRoute)
        .withFormUrlEncodedBody(("value", country2.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.get(OfficeOfTransitPage(index)) mustNot be(defined)
      userAnswersCaptor.getValue.get(OfficeOfTransitPage(Index(1))) mustNot be(defined)

    }

    "must redirect to add another country of routing  and not remove officeOfTransits if the country is changed to the same country" in {

      val userAnswers = emptyUserAnswers
        .setValue(CountryOfRoutingPage(index), countryFrance)
        .setValue(OfficeOfTransitPage(index), CustomsOffice("id0", "port37", None, country1.code.code))
        .setValue(OfficeOfTransitPage(Index(1)), CustomsOffice("id1", "port1", None, countryFrance.code.code))
        .setValue(OfficeOfTransitPage(Index(2)), CustomsOffice("id2", "port2", None, countryFrance.code.code))

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      when(mockCountriesService.getCountryCodesCTC()(any())).thenReturn(Future.successful(countryList))
      when(mockCountriesService.getCustomsSecurityAgreementAreaCountries()(any())).thenReturn(Future.successful(countryList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, countryOfRoutingRoute)
        .withFormUrlEncodedBody(("value", countryFrance.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.get(OfficeOfTransitPage(index)) must be(defined)
      userAnswersCaptor.getValue.get(OfficeOfTransitPage(Index(1))) must be(defined)
      userAnswersCaptor.getValue.get(OfficeOfTransitPage(Index(2))) must be(defined)

    }

  }
}
