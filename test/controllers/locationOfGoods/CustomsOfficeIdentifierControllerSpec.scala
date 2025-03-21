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
import forms.SelectableFormProvider
import forms.SelectableFormProvider.OfficeFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import navigation.LocationOfGoodsNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.external.OfficeOfDeparturePage
import pages.locationOfGoods.CustomsOfficeIdentifierPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CustomsOfficesService
import views.html.locationOfGoods.CustomsOfficeIdentifierView

import scala.concurrent.Future

class CustomsOfficeIdentifierControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val customsOffice1    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOffice2    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOfficeList = SelectableList(Seq(customsOffice1, customsOffice2))

  private val formProvider = new OfficeFormProvider()
  private val form         = formProvider.apply("locationOfGoods.customsOfficeIdentifier", customsOfficeList)
  private val field        = formProvider.field
  private val mode         = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]
  private lazy val customsOfficeIdentifierRoute                = routes.CustomsOfficeIdentifierController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[LocationOfGoodsNavigatorProvider]).toInstance(fakeLocationOfGoodsNavigatorProvider))
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  "CustomsOfficeIdentifier Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(OfficeOfDeparturePage, customsOffice1)
      when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, customsOfficeIdentifierRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsOfficeIdentifierView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, customsOfficeList.values, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      val userAnswers = emptyUserAnswers
        .setValue(OfficeOfDeparturePage, customsOffice1)
        .setValue(CustomsOfficeIdentifierPage, customsOffice1)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, customsOfficeIdentifierRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map(field -> customsOffice1.id))

      val view = injector.instanceOf[CustomsOfficeIdentifierView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, customsOfficeList.values, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(OfficeOfDeparturePage, customsOffice1)
      when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, customsOfficeIdentifierRoute)
        .withFormUrlEncodedBody((field, customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(OfficeOfDeparturePage, customsOffice1)
      when(mockCustomsOfficesService.getCustomsOfficesOfDepartureForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, customsOfficeIdentifierRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[CustomsOfficeIdentifierView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, customsOfficeList.values, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, customsOfficeIdentifierRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, customsOfficeIdentifierRoute)
        .withFormUrlEncodedBody((field, customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
