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

package controllers.transit.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.routing.CountryOfDestinationPage
import pages.transit.index.{InferredOfficeOfTransitCountryPage, OfficeOfTransitCountryPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.transit.index.NoOfficesAvailableView

class NoOfficesAvailableControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private lazy val noOfficesAvailableRoute = routes.NoOfficesAvailableController.onPageLoad(lrn, index).url

  "NoOfficesAvailableController" - {

    "must return OK and the correct view for a GET" - {
      "when transit country defined at index" in {
        forAll(arbitrary[Country], arbitrary[Country]) {
          (destinationCounty, transitCountry) =>
            val userAnswers = emptyUserAnswers
              .setValue(CountryOfDestinationPage, destinationCounty)
              .setValue(OfficeOfTransitCountryPage(index), transitCountry)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(GET, noOfficesAvailableRoute)

            val result = route(app, request).value

            val view = injector.instanceOf[NoOfficesAvailableView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(lrn, transitCountry)(request, messages).toString
        }
      }

      "when inferred transit country defined at index" in {
        forAll(arbitrary[Country], arbitrary[Country]) {
          (destinationCounty, transitCountry) =>
            val userAnswers = emptyUserAnswers
              .setValue(CountryOfDestinationPage, destinationCounty)
              .setValue(InferredOfficeOfTransitCountryPage(index), transitCountry)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(GET, noOfficesAvailableRoute)

            val result = route(app, request).value

            val view = injector.instanceOf[NoOfficesAvailableView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(lrn, transitCountry)(request, messages).toString
        }
      }

      "when only the destination country defined" in {
        forAll(arbitrary[Country]) {
          destinationCounty =>
            val userAnswers = emptyUserAnswers
              .setValue(CountryOfDestinationPage, destinationCounty)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(GET, noOfficesAvailableRoute)

            val result = route(app, request).value

            val view = injector.instanceOf[NoOfficesAvailableView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(lrn, destinationCounty)(request, messages).toString
        }
      }
    }

    "must redirect to Technical Difficulties for a GET if no country data is found" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, noOfficesAvailableRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.technicalDifficultiesUrl
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, noOfficesAvailableRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
