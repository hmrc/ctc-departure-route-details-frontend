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
import models.{LocationType, NormalMode}
import navigation.LocationOfGoodsNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import pages.locationOfGoods.LocationTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.locationOfGoods.LocationTypeView
import org.scalacheck.Arbitrary.arbitrary
import services.LocationTypeService

import scala.concurrent.Future

class LocationTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {
  private val lt1                                          = arbitrary[LocationType].sample.value
  private val lt2                                          = arbitrary[LocationType].sample.value
  private val lts                                          = Seq(lt1, lt2)
  private val formProvider                                 = new EnumerableFormProvider()
  private val form                                         = formProvider[LocationType]("locationOfGoods.locationType", lts)
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
    when(mockLocationTypeService.getLocationTypes()(any())).thenReturn(Future.successful(lts))
  }

  "LocationType Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, lts, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(LocationTypePage, lt1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> lt1.toString))

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, lts, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, locationTypeRoute)
        .withFormUrlEncodedBody(("value", lt1.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, locationTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[LocationTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, lts, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, locationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, locationTypeRoute)
        .withFormUrlEncodedBody(("value", lt1.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
