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

package controllers.exit

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AddAnotherFormProvider
import generators.Generators
import models.{Index, NormalMode, UserAnswers}
import navigation.RouteDetailsNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.exit.AddAnotherOfficeOfExitPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewModels.ListItem
import viewModels.exit.AddAnotherOfficeOfExitViewModel
import viewModels.exit.AddAnotherOfficeOfExitViewModel.AddAnotherOfficeOfExitViewModelProvider
import views.html.exit.AddAnotherOfficeOfExitView

class AddAnotherOfficeOfExitControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherOfficeOfExitViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore(frontendAppConfig))

  private val mode = NormalMode

  private lazy val addAnotherOfficeOfExitRoute = routes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode).url

  private val mockViewModelProvider = mock[AddAnotherOfficeOfExitViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[RouteDetailsNavigatorProvider]).toInstance(fakeRouteDetailsNavigatorProvider))
      .overrides(bind(classOf[AddAnotherOfficeOfExitViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxOfficesOfExit - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxOfficesOfExit)(listItem)

  private val viewModel = arbitrary[AddAnotherOfficeOfExitViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherOfficeOfExitController" - {

    "redirect to correct start page in this sub-section" - {
      "when 0 offices of exit" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(emptyViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherOfficeOfExitRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherOfficeOfExitRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherOfficeOfExitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherOfficeOfExitRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherOfficeOfExitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherOfficeOfExitPage, true))

        val request = FakeRequest(GET, addAnotherOfficeOfExitRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherOfficeOfExitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherOfficeOfExitPage, true))

        val request = FakeRequest(GET, addAnotherOfficeOfExitRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherOfficeOfExitView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to office of exit country page at next index" in {
          when(mockViewModelProvider.apply(any(), any())(any(), any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherOfficeOfExitRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.exit.index.routes.OfficeOfExitCountryController.onPageLoad(lrn, Index(listItems.length), mode).url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
          userAnswersCaptor.getValue.get(AddAnotherOfficeOfExitPage).value mustEqual true
        }
      }

      "when no submitted" - {
        "must redirect to the next page" in {
          when(mockViewModelProvider.apply(any(), any())(any(), any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherOfficeOfExitRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
          userAnswersCaptor.getValue.get(AddAnotherOfficeOfExitPage).value mustEqual false
        }
      }
    }

    "when max limit reached" - {
      "must redirect to the next page" in {
        when(mockViewModelProvider.apply(any(), any())(any(), any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherOfficeOfExitRoute)
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

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherOfficeOfExitRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherOfficeOfExitView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherOfficeOfExitRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherOfficeOfExitRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }

}
