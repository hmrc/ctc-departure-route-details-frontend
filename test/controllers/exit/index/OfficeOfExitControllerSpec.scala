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
import forms.SelectableFormProvider
import forms.SelectableFormProvider.OfficeFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import navigation.OfficeOfExitNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.exit.index.{OfficeOfExitCountryPage, OfficeOfExitPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CustomsOfficesService
import views.html.exit.index.OfficeOfExitView

import scala.concurrent.Future

class OfficeOfExitControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val customsOffice1    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOffice2    = arbitraryCustomsOffice.arbitrary.sample.get
  private val customsOfficeList = SelectableList(Seq(customsOffice1, customsOffice2))
  private val country           = arbitraryCountry.arbitrary.sample.get

  private val formProvider = new OfficeFormProvider()
  private val form         = formProvider.apply("exit.index.officeOfExit", customsOfficeList, country.description)
  private val field        = formProvider.field
  private val mode         = NormalMode

  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]
  private lazy val officeOfExitRoute                           = routes.OfficeOfExitController.onPageLoad(lrn, index, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[OfficeOfExitNavigatorProvider]).toInstance(fakeOfficeOfExitNavigatorProvider))
      .overrides(bind(classOf[CustomsOfficesService]).toInstance(mockCustomsOfficesService))

  private val baseAnswers = emptyUserAnswers.setValue(OfficeOfExitCountryPage(index), country)

  "OfficeOfExit Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfExitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(GET, officeOfExitRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfExitView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, customsOfficeList.values, country.description, index, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfExitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

      val userAnswers = baseAnswers.setValue(OfficeOfExitPage(index), customsOffice1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, officeOfExitRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map(field -> customsOffice1.id))

      val view = injector.instanceOf[OfficeOfExitView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, customsOfficeList.values, country.description, index, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfExitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))
      when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(POST, officeOfExitRoute)
        .withFormUrlEncodedBody((field, customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfExitForCountry(any())(any())).thenReturn(Future.successful(customsOfficeList))

      setExistingUserAnswers(baseAnswers)

      val request   = FakeRequest(POST, officeOfExitRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[OfficeOfExitView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, customsOfficeList.values, country.description, index, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, officeOfExitRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, officeOfExitRoute)
        .withFormUrlEncodedBody((field, customsOffice1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
