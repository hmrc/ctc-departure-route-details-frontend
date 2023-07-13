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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider
import generators.Generators
import models.NormalMode
import models.reference.SpecificCircumstanceIndicator
import navigation.RouteDetailsNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.SpecificCircumstanceIndicatorPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SpecificCircumstanceIndicatorsService
import views.html.SpecificCircumstanceIndicatorView

import scala.concurrent.Future

class SpecificCircumstanceIndicatorControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val sci1 = arbitrary[SpecificCircumstanceIndicator].sample.value
  private val sci2 = arbitrary[SpecificCircumstanceIndicator].sample.value
  private val scis = Seq(sci1, sci2)

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("specificCircumstanceIndicator", scis)
  private val mode         = NormalMode

  private val mockSpecificCircumstanceIndicatorsService: SpecificCircumstanceIndicatorsService = mock[SpecificCircumstanceIndicatorsService]
  private lazy val specificCircumstanceIndicatorRoute                                          = routes.SpecificCircumstanceIndicatorController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[RouteDetailsNavigatorProvider]).toInstance(fakeRouteDetailsNavigatorProvider))
      .overrides(bind(classOf[SpecificCircumstanceIndicatorsService]).toInstance(mockSpecificCircumstanceIndicatorsService))

  "SpecificCircumstanceIndicator Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockSpecificCircumstanceIndicatorsService.getSpecificCircumstanceIndicators()(any())).thenReturn(Future.successful(scis))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, specificCircumstanceIndicatorRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[SpecificCircumstanceIndicatorView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, scis, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockSpecificCircumstanceIndicatorsService.getSpecificCircumstanceIndicators()(any())).thenReturn(Future.successful(scis))
      val userAnswers = emptyUserAnswers.setValue(SpecificCircumstanceIndicatorPage, sci1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, specificCircumstanceIndicatorRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> sci1.value))

      val view = injector.instanceOf[SpecificCircumstanceIndicatorView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, scis, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSpecificCircumstanceIndicatorsService.getSpecificCircumstanceIndicators()(any())).thenReturn(Future.successful(scis))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, specificCircumstanceIndicatorRoute)
        .withFormUrlEncodedBody(("value", sci1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockSpecificCircumstanceIndicatorsService.getSpecificCircumstanceIndicators()(any())).thenReturn(Future.successful(scis))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, specificCircumstanceIndicatorRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[SpecificCircumstanceIndicatorView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, scis, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, specificCircumstanceIndicatorRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, specificCircumstanceIndicatorRoute)
        .withFormUrlEncodedBody(("value", sci1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
