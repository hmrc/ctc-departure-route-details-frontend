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

package pages.routing.index

import models.Index
import models.reference.{Country, CountryCode}
import pages.behaviours.PageBehaviours
import pages.sections.exit.ExitSection
import pages.sections.transit.TransitSection
import play.api.libs.json.Json

class CountryOfRoutingPageSpec extends PageBehaviours {

  "CountryOfRoutingPage" - {

    beRetrievable[Country](CountryOfRoutingPage(index))

    beSettable[Country](CountryOfRoutingPage(index))

    beRemovable[Country](CountryOfRoutingPage(index))

    "cleanup" - {

      "must remove transit and exit sections" - {
        "when country of routing changes" in {
          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(Index(0)), Country(CountryCode("FR"), "France"))
            .setValue(TransitSection, Json.obj("foo" -> "bar"))
            .setValue(ExitSection, Json.obj("foo" -> "bar"))

          val result = userAnswers.setValue(CountryOfRoutingPage(Index(0)), Country(CountryCode("ES"), "Spain"))

          result.get(TransitSection) must not be defined
          result.get(ExitSection) must not be defined
        }

        "when country of routing added" in {
          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(Index(0)), Country(CountryCode("FR"), "France"))
            .setValue(TransitSection, Json.obj("foo" -> "bar"))
            .setValue(ExitSection, Json.obj("foo" -> "bar"))

          val result = userAnswers.setValue(CountryOfRoutingPage(Index(1)), Country(CountryCode("ES"), "Spain"))

          result.get(TransitSection) must not be defined
          result.get(ExitSection) must not be defined
        }
      }

      "must not remove transit and exit sections" - {
        "when country of routing doesn't change" in {
          val country = Country(CountryCode("FR"), "France")

          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(Index(0)), country)
            .setValue(TransitSection, Json.obj("foo" -> "bar"))
            .setValue(ExitSection, Json.obj("foo" -> "bar"))

          val result = userAnswers.setValue(CountryOfRoutingPage(Index(0)), country)

          result.get(TransitSection) must be(defined)
          result.get(ExitSection) must be(defined)
        }
      }
    }
  }
}
