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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.CountryFormProvider
import generators.Generators
import models.{CountryList, CustomsOfficeList, NormalMode}
import navigation.OfficeOfExitNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.exit.index.OfficeOfExitCountryPage
import pages.routing.CountryOfDestinationPage
import play.api.data.FormError
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CustomsOfficesService
import views.html.exit.index.OfficeOfExitCountryView

import scala.concurrent.Future

class OfficeOfExitCountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val country1    = arbitraryCountry.arbitrary.sample.get
  private val country2    = arbitraryCountry.arbitrary.sample.get
  private val countryList = CountryList(Seq(country1, country2))

  private val formProvider = new CountryFormProvider()
  private val form         = formProvider("exit.index.officeOfExitCountry", countryList)
  private val mode         = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]

  private lazy val officeOfExitCountryRoute = routes.OfficeOfExitCountryController.onPageLoad(lrn, index, mode).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCustomsOfficesService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[OfficeOfExitNavigatorProvider]).toInstance(fakeOfficeOfExitNavigatorProvider))
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  private val baseAnswers = emptyUserAnswers.setValue(CountryOfDestinationPage, country2)

  "OfficeOfExitCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCountriesService.getOfficeOfExitCountries(any(), any())(any()))
        .thenReturn(Future.successful(countryList))

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(GET, officeOfExitCountryRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfExitCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, countryList.countries, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCountriesService.getOfficeOfExitCountries(any(), any())(any()))
        .thenReturn(Future.successful(countryList))

      val userAnswers = baseAnswers.setValue(OfficeOfExitCountryPage(index), country1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, officeOfExitCountryRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> country1.code.code))

      val view = injector.instanceOf[OfficeOfExitCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, countryList.countries, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      when(mockCountriesService.getOfficeOfExitCountries(any(), any())(any()))
        .thenReturn(Future.successful(countryList))

      when(mockCustomsOfficesService.getCustomsOfficesOfExitForCountry(any())(any()))
        .thenReturn(Future.successful(arbitrary[CustomsOfficeList].sample.value))

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(POST, officeOfExitCountryRoute)
        .withFormUrlEncodedBody(("value", country1.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCountriesService.getOfficeOfExitCountries(any(), any())(any()))
        .thenReturn(Future.successful(countryList))

      setExistingUserAnswers(baseAnswers)

      val request   = FakeRequest(POST, officeOfExitCountryRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfExitCountryView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, countryList.countries, index, mode)(request, messages).toString
    }

    "must return a Bad Request and errors when submitted country has no corresponding customs offices" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      when(mockCountriesService.getOfficeOfExitCountries(any(), any())(any()))
        .thenReturn(Future.successful(countryList))

      when(mockCustomsOfficesService.getCustomsOfficesOfExitForCountry(any())(any()))
        .thenReturn(Future.successful(CustomsOfficeList(Nil)))

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(POST, officeOfExitCountryRoute).withFormUrlEncodedBody(("value", country1.code.code))

      val boundForm = form
        .withError(FormError("value", "You cannot use this country as it does not have any offices of exit"))

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfExitCountryView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, countryList.countries, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, officeOfExitCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, officeOfExitCountryRoute)
        .withFormUrlEncodedBody(("value", country1.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
