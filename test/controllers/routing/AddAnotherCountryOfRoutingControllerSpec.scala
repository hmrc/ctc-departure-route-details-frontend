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
import config.Constants.SecurityType.NoSecurityDetails
import forms.AddAnotherFormProvider
import generators.Generators
import models.reference.{Country, CountryCode}
import models.{Index, NormalMode, UserAnswers}
import navigation.RoutingNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.routing.{AddAnotherCountryOfRoutingPage, BindingItineraryPage}
import pages.routing.index.CountryOfRoutingPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.ListItem
import viewModels.routing.AddAnotherCountryOfRoutingViewModel
import viewModels.routing.AddAnotherCountryOfRoutingViewModel.AddAnotherCountryOfRoutingViewModelProvider
import views.html.routing.AddAnotherCountryOfRoutingView

import scala.concurrent.Future

class AddAnotherCountryOfRoutingControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherCountryOfRoutingViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore(frontendAppConfig))

  private val mode = NormalMode

  private lazy val addAnotherCountryOfRoutingRoute = routes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode).url

  private val mockViewModelProvider = mock[AddAnotherCountryOfRoutingViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[RoutingNavigatorProvider]).toInstance(fakeRoutingNavigatorProvider))
      .overrides(bind(classOf[AddAnotherCountryOfRoutingViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxCountriesOfRouting - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxCountriesOfRouting)(listItem)

  private val viewModel = arbitrary[AddAnotherCountryOfRoutingViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherCountryOfRoutingController" - {

    "redirect to binding itinerary page" - {
      "when 0 countries" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(emptyViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherCountryOfRoutingRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.BindingItineraryController.onPageLoad(lrn, mode).url
      }
    }

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {

        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherCountryOfRoutingRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherCountryOfRoutingView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {

        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherCountryOfRoutingRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherCountryOfRoutingView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered " - {
      "when max limit not reached " in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherCountryOfRoutingPage, true))

        val request = FakeRequest(GET, addAnotherCountryOfRoutingRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherCountryOfRoutingView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached " in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherCountryOfRoutingPage, true))

        val request = FakeRequest(GET, addAnotherCountryOfRoutingRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherCountryOfRoutingView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to guarantee type page at next index" in {
          when(mockViewModelProvider.apply(any(), any())(any(), any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherCountryOfRoutingRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routing.index.routes.CountryOfRoutingController.onPageLoad(lrn, mode, Index(listItems.length)).url

          val userAnswerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswerCaptor.capture())(any())
          userAnswerCaptor.getValue.get(AddAnotherCountryOfRoutingPage).value mustEqual true

        }
      }

      "when no submitted" - {
        "must redirect to check your answers" in {
          when(mockViewModelProvider.apply(any(), any())(any(), any()))
            .thenReturn(notMaxedOutViewModel)

          when(mockSessionRepository.set(any())(any()))
            .thenReturn(Future.successful(true))

          val ua = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(BindingItineraryPage, true)
            .setValue(CountryOfRoutingPage(Index(0)), Country(CountryCode("GB"), "description"))

          setExistingUserAnswers(ua)

          val request = FakeRequest(POST, addAnotherCountryOfRoutingRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          val userAnswerCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswerCaptor.capture())(any())
          userAnswerCaptor.getValue.get(AddAnotherCountryOfRoutingPage).value mustEqual false
        }
      }
    }

    "when max limit reached" - {
      "must redirect to check your answers" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(maxedOutViewModel)

        when(mockSessionRepository.set(any())(any()))
          .thenReturn(Future.successful(true))

        val ua = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(BindingItineraryPage, true)
          .setValue(CountryOfRoutingPage(Index(0)), Country(CountryCode("GB"), "description"))

        setExistingUserAnswers(ua)

        val request = FakeRequest(POST, addAnotherCountryOfRoutingRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(notMaxedOutViewModel)

        when(mockSessionRepository.set(any())(any()))
          .thenReturn(Future.successful(true))

        val ua = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(BindingItineraryPage, true)
          .setValue(CountryOfRoutingPage(Index(0)), Country(CountryCode("GB"), "description"))

        setExistingUserAnswers(ua)

        val request = FakeRequest(POST, addAnotherCountryOfRoutingRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherCountryOfRoutingView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherCountryOfRoutingRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherCountryOfRoutingRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
