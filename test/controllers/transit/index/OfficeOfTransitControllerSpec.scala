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

package controllers.transit.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.CustomsOfficeForCountryFormProvider
import generators.Generators
import models.reference.{Country, CustomsOffice}
import models.{reference, CustomsOfficeList, NormalMode}
import navigation.OfficeOfTransitNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.routing.CountryOfDestinationPage
import pages.transit.index.OfficeOfTransitPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CustomsOfficesService
import views.html.transit.index.OfficeOfTransitView

import scala.concurrent.Future

class OfficeOfTransitControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val customsOffice1     = arbitrary[reference.CustomsOffice].sample.value
  private val customsOffice2     = arbitrary[CustomsOffice].sample.value
  private val customsOfficeList  = CustomsOfficeList(Seq(customsOffice1, customsOffice2))
  private val country            = arbitrary[Country].sample.value
  private val destinationCountry = arbitrary[Country].sample.value

  private val formProvider           = new CustomsOfficeForCountryFormProvider()
  private def form(country: Country) = formProvider("transit.index.officeOfTransit", customsOfficeList, country.description)
  private val mode                   = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]
  private lazy val officeOfTransitRoute                        = routes.OfficeOfTransitController.onPageLoad(lrn, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[OfficeOfTransitNavigatorProvider]).toInstance(fakeOfficeOfTransitNavigatorProvider))
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  "OfficeOfTransit Controller" - {

    "must return OK and the correct view for a GET" - {
      "when country defined at index" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        setExistingUserAnswers(emptyUserAnswers.setOfficeOfTransitCountry(country))

        val request = FakeRequest(GET, officeOfTransitRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(country), lrn, customsOfficeList.customsOffices, country.description, mode, index)(request, messages).toString
      }

      "when only country of destination defined" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers.setValue(CountryOfDestinationPage, destinationCountry)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, officeOfTransitRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(destinationCountry), lrn, customsOfficeList.customsOffices, destinationCountry.description, mode, index)(request, messages).toString
      }

      "when country defined at index and country of destination defined" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfDestinationPage, destinationCountry)
          .setOfficeOfTransitCountry(country)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, officeOfTransitRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(country), lrn, customsOfficeList.customsOffices, country.description, mode, index)(request, messages).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when country defined at index" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers
          .setOfficeOfTransitCountry(country)
          .setValue(OfficeOfTransitPage(index), customsOffice1)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, officeOfTransitRoute)

        val result = route(app, request).value

        val filledForm = form(country).bind(Map("value" -> customsOffice1.id))

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, customsOfficeList.customsOffices, country.description, mode, index)(request, messages).toString
      }

      "when only country of destination defined" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfDestinationPage, destinationCountry)
          .setValue(OfficeOfTransitPage(index), customsOffice1)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, officeOfTransitRoute)

        val result = route(app, request).value

        val filledForm = form(destinationCountry).bind(Map("value" -> customsOffice1.id))

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, customsOfficeList.customsOffices, destinationCountry.description, mode, index)(request, messages).toString
      }

      "when country defined at index and country of destination defined" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfDestinationPage, destinationCountry)
          .setOfficeOfTransitCountry(country)
          .setValue(OfficeOfTransitPage(index), customsOffice1)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, officeOfTransitRoute)

        val result = route(app, request).value

        val filledForm = form(country).bind(Map("value" -> customsOffice1.id))

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, customsOfficeList.customsOffices, country.description, mode, index)(request, messages).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers.setOfficeOfTransitCountry(country))

      val request = FakeRequest(POST, officeOfTransitRoute)
        .withFormUrlEncodedBody(("value", customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" - {
      "when country defined at index" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        setExistingUserAnswers(emptyUserAnswers.setOfficeOfTransitCountry(country))

        val request   = FakeRequest(POST, officeOfTransitRoute).withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form(country).bind(Map("value" -> "invalid value"))

        val result = route(app, request).value

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, customsOfficeList.customsOffices, country.description, mode, index)(request, messages).toString
      }

      "when only country of destination defined" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers.setValue(CountryOfDestinationPage, destinationCountry)
        setExistingUserAnswers(userAnswers)

        val request   = FakeRequest(POST, officeOfTransitRoute).withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form(destinationCountry).bind(Map("value" -> "invalid value"))

        val result = route(app, request).value

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, customsOfficeList.customsOffices, destinationCountry.description, mode, index)(request, messages).toString
      }

      "when country defined at index and country of destination defined" in {
        when(mockCustomsOfficesService.getCustomsOfficesOfTransitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfDestinationPage, destinationCountry)
          .setOfficeOfTransitCountry(country)

        setExistingUserAnswers(userAnswers)

        val request   = FakeRequest(POST, officeOfTransitRoute).withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form(country).bind(Map("value" -> "invalid value"))

        val result = route(app, request).value

        val view = injector.instanceOf[OfficeOfTransitView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, customsOfficeList.customsOffices, country.description, mode, index)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, officeOfTransitRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, officeOfTransitRoute)
        .withFormUrlEncodedBody(("value", customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
