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
import models.reference.{LocationOfGoodsIdentification, LocationType}
import models.{NormalMode, UserAnswers}
import navigation.LocationOfGoodsNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.{IdentificationPage, InferredIdentificationPage, InferredLocationTypePage, LocationTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.LocationOfGoodsIdentificationTypeService
import views.html.locationOfGoods.IdentificationView

import scala.concurrent.Future

class IdentificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val ids = arbitrary[Seq[LocationOfGoodsIdentification]].sample.value
  private val id1 = ids.head

  private val formProvider             = new EnumerableFormProvider()
  private val form                     = formProvider[LocationOfGoodsIdentification]("locationOfGoods.identification", ids)
  private val mode                     = NormalMode
  private lazy val identificationRoute = routes.IdentificationController.onPageLoad(lrn, mode).url
  private val mockLocationIdentifierService: LocationOfGoodsIdentificationTypeService = mock[LocationOfGoodsIdentificationTypeService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[LocationOfGoodsNavigatorProvider]).toInstance(fakeLocationOfGoodsNavigatorProvider))
      .overrides(bind(classOf[LocationOfGoodsIdentificationTypeService]).toInstance(mockLocationIdentifierService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLocationIdentifierService)
    when(mockLocationIdentifierService.getLocationOfGoodsIdentificationTypes(any())(any())).thenReturn(Future.successful(ids))
  }

  private val page            = Gen.oneOf(LocationTypePage, InferredLocationTypePage).sample.value
  private val locationType    = arbitrary[LocationType].sample.value
  private val baseUserAnswers = emptyUserAnswers.setValue(page, locationType)

  "LocationOfGoodsIdentification Controller" - {

    "when value is inferred" - {
      "must redirect to next page" in {
        when(mockLocationIdentifierService.getLocationOfGoodsIdentificationTypes(any())(any())).thenReturn(Future.successful(Seq(id1)))

        setExistingUserAnswers(baseUserAnswers)

        val request = FakeRequest(GET, identificationRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.getValue(InferredIdentificationPage) mustBe id1
        userAnswersCaptor.getValue.get(IdentificationPage) must not be defined
      }

      "must return OK and the correct view for a GET" in {

        when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

        setExistingUserAnswers(baseUserAnswers)

        val request = FakeRequest(GET, identificationRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[IdentificationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, lrn, ids, mode)(request, messages).toString
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.setValue(IdentificationPage, id1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> id1.code))

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, ids, mode)(request, messages).toString

    }
    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

      setExistingUserAnswers(baseUserAnswers)

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", id1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(baseUserAnswers)

      val request   = FakeRequest(POST, identificationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, ids, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", id1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }

}
