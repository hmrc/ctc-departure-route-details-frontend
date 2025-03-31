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
import forms.SelectableFormProvider.CountryFormProvider
import generators.Generators
import models.reference.{Country, CountryCode}
import models.{NormalMode, SelectableList, UserAnswers}
import navigation.CountryOfRoutingNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.routing.index.CountryOfRoutingPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.routing.index.CountryOfRoutingView

import scala.concurrent.Future

class CountryOfRoutingControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private val country1      = arbitraryCountry.arbitrary.sample.get
  private val country2      = arbitraryCountry.arbitrary.sample.get
  private val countryFrance = Country(CountryCode("FR"), "France")
  private val countryList   = SelectableList(Seq(country1, country2, countryFrance))

  private val formProvider = new CountryFormProvider()
  private val form         = formProvider.apply("routing.index.countryOfRouting", countryList)
  private val field        = formProvider.field
  private val mode         = NormalMode

  private lazy val countryOfRoutingRoute = routes.CountryOfRoutingController.onPageLoad(lrn, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[CountryOfRoutingNavigatorProvider]).toInstance(fakeCountryOfRoutingNavigatorProvider))

  "CountryOfRouting Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCountriesService.getCountriesOfRouting(any(), any())(any())).thenReturn(Future.successful(countryList))
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

      val filledForm = form.bind(Map(field -> country1.code.code))

      val view = injector.instanceOf[CountryOfRoutingView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, countryList.values, mode, index)(request, messages).toString
    }
    "must redirect to the next page when valid data is submitted" in {
      forAll(arbitrary[Boolean], arbitrary[Boolean]) {
        (isInCL112, isInCL147) =>
          beforeEach()

          when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
          when(mockCountriesService.isInCL112(any())(any())).thenReturn(Future.successful(isInCL112))
          when(mockCountriesService.isInCL147(any())(any())).thenReturn(Future.successful(isInCL147))
          when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, countryOfRoutingRoute)
            .withFormUrlEncodedBody((field, country1.code.code))

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
               |            "isInCL112" : $isInCL112,
               |            "isInCL147" : $isInCL147
               |          }
               |        }
               |      ]
               |    }
               |  }
               |}
               |""".stripMargin)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, countryOfRoutingRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[CountryOfRoutingView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, countryList.values, mode, index)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, countryOfRoutingRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, countryOfRoutingRoute)
        .withFormUrlEncodedBody((field, country1.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
