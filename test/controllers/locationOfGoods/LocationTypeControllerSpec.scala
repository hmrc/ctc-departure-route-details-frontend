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

package controllers.locationOfGoods

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.EnumerableFormProvider
import generators.Generators
import models.ProcedureType.{Normal, Simplified}
import models.reference.LocationType
import models.{NormalMode, UserAnswers}
import navigation.LocationOfGoodsNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.external.ProcedureTypePage
import pages.locationOfGoods.{InferredLocationTypePage, LocationTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.LocationTypeService
import views.html.locationOfGoods.LocationTypeView

import scala.concurrent.Future

class LocationTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val lts = arbitrary[Seq[LocationType]].sample.value
  private val lt  = lts.head

  private val formProvider                                 = new EnumerableFormProvider()
  private val form                                         = formProvider("locationOfGoods.locationType", lts)
  private val mode                                         = NormalMode
  private lazy val locationTypeRoute                       = routes.LocationTypeController.onPageLoad(lrn, mode).url
  private val mockLocationTypeService: LocationTypeService = mock[LocationTypeService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[LocationOfGoodsNavigatorProvider]).toInstance(fakeLocationOfGoodsNavigatorProvider))
      .overrides(bind(classOf[LocationTypeService]).toInstance(mockLocationTypeService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLocationTypeService)
    when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(lts))
  }

  "LocationType Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(ProcedureTypePage, Normal)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, lts, mode)(request, messages).toString
    }

    "must redirect to the next page and infer LocationType when only one location type" in {

      when(mockLocationTypeService.getLocationTypes(any())(any())).thenReturn(Future.successful(Seq(lt)))

      val userAnswers = emptyUserAnswers.setValue(ProcedureTypePage, Simplified)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.get(LocationTypePage) must not be defined
      userAnswersCaptor.getValue.getValue(InferredLocationTypePage) mustBe lt
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(ProcedureTypePage, Normal)
        .setValue(LocationTypePage, lt)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> lt.code))

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, lts, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(ProcedureTypePage, Normal)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, locationTypeRoute)
        .withFormUrlEncodedBody(("value", lt.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(ProcedureTypePage, Normal)
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, locationTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, lts, mode)(request, messages).toString
      view(boundForm, lrn, lts, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, locationTypeRoute)
        .withFormUrlEncodedBody(("value", lt.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
