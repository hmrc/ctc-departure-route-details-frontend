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
import controllers.routing.{routes => routingRoutes}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.{Country, CountryCode, CustomsOffice}
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.exit.index.OfficeOfExitPage
import pages.routing.index.CountryOfRoutingPage
import pages.sections.routing.CountryOfRoutingSection
import pages.transit.index.OfficeOfTransitPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.routing.index.RemoveCountryOfRoutingYesNoView

import scala.concurrent.Future

class RemoveCountryOfRoutingYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider           = new YesNoFormProvider()
  private def form(country: Country) = formProvider("routing.index.removeCountryOfRoutingYesNo", country.toString)

  private val mode = NormalMode

  private lazy val removeCountryOfROutingYesNoRoute = routes.RemoveCountryOfRoutingYesNoController.onPageLoad(lrn, mode, index).url

  "RemoveCountryOfRoutingYesNoController" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitraryCountryOfRoutingAnswers(emptyUserAnswers, index)) {
        answers =>
          setExistingUserAnswers(answers)
          val country = answers.getValue(CountryOfRoutingPage(index))

          val request = FakeRequest(GET, removeCountryOfROutingYesNoRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[RemoveCountryOfRoutingYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form(country), lrn, mode, index, country)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another country of routing and remove country at specified index" in {
        forAll(arbitraryCountryOfRoutingAnswers(emptyUserAnswers, index)) {
          answers =>
            reset(mockSessionRepository)
            when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

            setExistingUserAnswers(answers)

            val request = FakeRequest(POST, removeCountryOfROutingYesNoRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
            userAnswersCaptor.getValue.get(CountryOfRoutingSection(index)) mustNot be(defined)
        }
      }

      "must redirect to add another country of routing and remove country at specified index and remove officeOfTransit and officeOfExit" in {

        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingPage(index), Country(CountryCode("FR"), "France"))
          .setValue(OfficeOfTransitPage(index), CustomsOffice("GB", "Britain", "GB"))
          .setValue(OfficeOfTransitPage(Index(1)), CustomsOffice("FR", "FR", "FR"))
          .setValue(OfficeOfTransitPage(Index(2)), CustomsOffice("FR", "FR", "FR"))
          .setValue(OfficeOfExitPage(index), CustomsOffice("GB", "Britain", "GB"))
          .setValue(OfficeOfExitPage(Index(1)), CustomsOffice("FR", "FR", "FR"))
          .setValue(OfficeOfExitPage(Index(2)), CustomsOffice("FR", "FR", "FR"))

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeCountryOfROutingYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.get(CountryOfRoutingSection(index)) mustNot be(defined)
        userAnswersCaptor.getValue.get(OfficeOfTransitPage(index)) must be(defined)
        userAnswersCaptor.getValue.get(OfficeOfTransitPage(Index(1))) mustNot be(defined)
        userAnswersCaptor.getValue.get(OfficeOfTransitPage(Index(2))) mustNot be(defined)
        userAnswersCaptor.getValue.get(OfficeOfExitPage(index)) must be(defined)
        userAnswersCaptor.getValue.get(OfficeOfExitPage(Index(1))) mustNot be(defined)
        userAnswersCaptor.getValue.get(OfficeOfExitPage(Index(2))) mustNot be(defined)

      }
    }

    "when no submitted" - {
      "must redirect to add another country and not remove country at specified index" in {
        forAll(arbitraryCountryOfRoutingAnswers(emptyUserAnswers, index)) {
          answers =>
            reset(mockSessionRepository)

            setExistingUserAnswers(answers)

            val request = FakeRequest(POST, removeCountryOfROutingYesNoRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode).url

            verify(mockSessionRepository, never()).set(any())(any())
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      forAll(arbitraryCountryOfRoutingAnswers(emptyUserAnswers, index)) {
        answers =>
          setExistingUserAnswers(answers)
          val country = answers.getValue(CountryOfRoutingPage(index))

          setExistingUserAnswers(answers)

          val request   = FakeRequest(POST, removeCountryOfROutingYesNoRoute).withFormUrlEncodedBody(("value", ""))
          val boundForm = form(country).bind(Map("value" -> ""))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveCountryOfRoutingYesNoView]

          contentAsString(result) mustEqual
            view(boundForm, lrn, mode, index, country)(request, messages).toString
      }
    }

    "must redirect for a GET" - {
      "when no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeCountryOfROutingYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when country not found at index" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeCountryOfROutingYesNoRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode).url
      }
    }

    "must redirect for a POST" - {
      "when no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeCountryOfROutingYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when country not found at index" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeCountryOfROutingYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode).url
      }
    }
  }
}
