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

package config

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.Helpers.running

class FrontendAppConfigSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with AppWithDefaultMockFixtures {

  "dates" - {

    "officeOfTransitETA" - {

      "in Transition" - {

        val app = transitionApplicationBuilder().build()

        val config = app.injector.instanceOf[FrontendAppConfig]

        "etaDateDaysBefore must be 44" in {

          running(app) {
            config.etaDateDaysBefore mustBe 44
          }
        }

        "etaDateDaysAfter must be 60" in {

          running(app) {
            config.etaDateDaysAfter mustBe 60
          }
        }

      }

      "in Post-Transition" - {

        val app = postTransitionApplicationBuilder().build()

        val config = app.injector.instanceOf[FrontendAppConfig]

        "etaDateDaysBefore must be 0" in {

          running(app) {
            config.etaDateDaysBefore mustBe 0
          }
        }

        "etaDateDaysAfter must be 60" in {

          running(app) {
            config.etaDateDaysAfter mustBe 60
          }
        }
      }
    }
  }
}
