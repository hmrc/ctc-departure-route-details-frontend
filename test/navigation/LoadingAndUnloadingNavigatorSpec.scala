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

package navigation

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage

class LoadingAndUnloadingNavigatorSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "LoadingAndUnloading Navigator" - {

    "when in NormalMode" - {

      val mode              = NormalMode
      val navigatorProvider = new LoadingAndUnloadingNavigatorProviderImpl()
      val navigator         = navigatorProvider.apply(mode)

      "when answers complete" - {
        "must redirect to route check your answers" in {

          val securityType   = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value
          val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityType)

          forAll(arbitraryLoadingAndUnloadingAnswers(initialAnswers)) {
            answers =>
              navigator
                .nextPage(answers, None)
                .mustEqual(controllers.loadingAndUnloading.routes.LoadingAndUnloadingAnswersController.onPageLoad(answers.lrn, mode))
          }
        }
      }
    }

    "when in CheckMode" - {

      val mode              = CheckMode
      val navigatorProvider = new LoadingAndUnloadingNavigatorProviderImpl()
      val navigator         = navigatorProvider.apply(mode)

      "when answers complete" - {
        "must redirect to route details check your answers" in {
          forAll(arbitraryRouteDetailsAnswers(emptyUserAnswers)) {
            answers =>
              navigator
                .nextPage(answers, None)
                .mustEqual(controllers.routes.RouteDetailsAnswersController.onPageLoad(answers.lrn))
          }
        }
      }
    }
  }
}
