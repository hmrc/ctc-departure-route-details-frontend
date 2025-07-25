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

package controllers.routing

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider
import forms.SelectableFormProvider.OfficeFormProvider
import generators.Generators
import models.reference.{Country, CountryCode, CustomsOffice}
import models.{NormalMode, SelectableList, UserAnswers}
import navigation.RoutingNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.routing.{CountryOfDestinationPage, OfficeOfDestinationPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CustomsOfficesService
import views.html.routing.OfficeOfDestinationView

import scala.concurrent.Future

class OfficeOfDestinationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private val customsOffice1    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOffice2    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOfficeList = SelectableList(Seq(customsOffice1, customsOffice2))
  private val country           = Country(CountryCode("FR"), "France")

  private val formProvider = new OfficeFormProvider()
  private val form         = formProvider.apply("routing.officeOfDestination", customsOfficeList)
  private val field        = formProvider.field
  private val mode         = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]
  private lazy val officeOfDestinationRoute                    = routes.OfficeOfDestinationController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[RoutingNavigatorProvider]).toInstance(fakeRoutingNavigatorProvider))
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  "OfficeOfDestination Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfDestinationForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      val updatedUserAnswers = emptyUserAnswers.setValue(CountryOfDestinationPage, country)
      setExistingUserAnswers(updatedUserAnswers)

      val request = FakeRequest(GET, officeOfDestinationRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfDestinationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, country.description, customsOfficeList.values, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfDestinationForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      val userAnswers = emptyUserAnswers
        .setValue(CountryOfDestinationPage, country)
        .setValue(OfficeOfDestinationPage, customsOffice1)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, officeOfDestinationRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map(field -> customsOffice1.id))

      val view = injector.instanceOf[OfficeOfDestinationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, country.description, customsOfficeList.values, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      forAll(arbitrary[Boolean]) {
        isInCL112 =>
          beforeEach()

          val customsOffice = CustomsOffice("FR123", "name", "FR")
          when(mockCustomsOfficesService.getCustomsOfficesOfDestinationForCountry(any())(any()))
            .thenReturn(Future.successful(SelectableList(Seq(customsOffice))))
          when(mockCountriesService.isInCL112(any())(any())).thenReturn(Future.successful(isInCL112))
          when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

          val userAnswers = emptyUserAnswers.setValue(CountryOfDestinationPage, country)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(POST, officeOfDestinationRoute)
            .withFormUrlEncodedBody((field, customsOffice.id))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
          userAnswersCaptor.getValue.data mustEqual Json.parse(s"""
               |{
               |  "routeDetails" : {
               |    "routing" : {
               |      "countryOfDestination" : {
               |        "code" : "${country.code.code}",
               |        "description" : "${country.description}"
               |      },
               |      "officeOfDestination" : {
               |        "id" : "${customsOffice.id}",
               |        "name" : "${customsOffice.name}",
               |        "countryId" : "${customsOffice.countryId}",
               |        "isInCL112" : $isInCL112
               |      }
               |    }
               |  }
               |}
               |""".stripMargin)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfDestinationForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      val userAnswers = emptyUserAnswers
        .setValue(CountryOfDestinationPage, country)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, officeOfDestinationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfDestinationView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, country.description, customsOfficeList.values, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, officeOfDestinationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, officeOfDestinationRoute)
        .withFormUrlEncodedBody((field, customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
