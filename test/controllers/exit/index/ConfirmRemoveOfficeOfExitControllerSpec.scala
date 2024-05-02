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
import forms.YesNoFormProvider
import generators.Generators
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.exit.OfficeOfExitSection
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.exit.RemoveOfficeOfExitViewModel
import viewModels.exit.RemoveOfficeOfExitViewModel.RemoveOfficeOfExitViewModelProvider
import views.html.exit.index.ConfirmRemoveOfficeOfExitView

import scala.concurrent.Future

class ConfirmRemoveOfficeOfExitControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private val viewModel = arbitrary[RemoveOfficeOfExitViewModel].sample.value

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider(viewModel.prefix, viewModel.args: _*)

  private val mode = NormalMode

  private val mockViewModelProvider = mock[RemoveOfficeOfExitViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[RemoveOfficeOfExitViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any(), any())).thenReturn(viewModel)
  }

  private def confirmRemoveOfficeOfExitRoute(index: Index) = routes.ConfirmRemoveOfficeOfExitController.onPageLoad(lrn, index, mode).url

  "ConfirmRemoveOfficeOfExit Controller" - {

    "must return OK and the correct view for a GET" - {
      "when office of exit name has been answered" in {
        forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, index)) {
          userAnswers =>
            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(GET, confirmRemoveOfficeOfExitRoute(index))
            val result  = route(app, request).value

            val view = injector.instanceOf[ConfirmRemoveOfficeOfExitView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(form, lrn, index, mode, viewModel)(request, messages).toString
        }
      }
    }

    "when yes submitted" - {
      "must redirect to add another office of exit and remove office of exit at specified index" in {
        forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, index)) {
          answers =>
            reset(mockSessionRepository)
            when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

            setExistingUserAnswers(answers)

            val request = FakeRequest(POST, confirmRemoveOfficeOfExitRoute(index))
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.exit.routes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
            userAnswersCaptor.getValue.get(OfficeOfExitSection(index)) mustNot be(defined)
        }
      }
    }

    "when no submitted" - {
      "must redirect to add another office of exit and not remove office of exit at specified index" in {
        forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, index)) {
          answers =>
            reset(mockSessionRepository)

            setExistingUserAnswers(answers)

            val request = FakeRequest(POST, confirmRemoveOfficeOfExitRoute(index))
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.exit.routes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode).url

            verify(mockSessionRepository, never()).set(any())(any())
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" - {
      "when office of exit name has been answered" in {
        forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, index)) {
          userAnswers =>
            setExistingUserAnswers(userAnswers)

            val request   = FakeRequest(POST, confirmRemoveOfficeOfExitRoute(index)).withFormUrlEncodedBody(("value", ""))
            val boundForm = form.bind(Map("value" -> ""))

            val result = route(app, request).value

            status(result) mustEqual BAD_REQUEST

            val view = injector.instanceOf[ConfirmRemoveOfficeOfExitView]

            val content = contentAsString(result)

            content mustEqual
              view(boundForm, lrn, index, mode, viewModel)(request, messages).toString
        }
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, confirmRemoveOfficeOfExitRoute(index))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to add another for a GET if no existing data is found at given index" in {
      forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, Index(0))) {
        answers =>
          setExistingUserAnswers(answers)

          val request = FakeRequest(GET, confirmRemoveOfficeOfExitRoute(Index(1)))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.exit.routes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode).url
      }
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, confirmRemoveOfficeOfExitRoute(index))
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to add another for a POST if no existing data is found at given index" in {
      forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, Index(0))) {
        answers =>
          setExistingUserAnswers(answers)

          val request = FakeRequest(POST, confirmRemoveOfficeOfExitRoute(Index(1)))
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.exit.routes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode).url
      }
    }
  }
}
