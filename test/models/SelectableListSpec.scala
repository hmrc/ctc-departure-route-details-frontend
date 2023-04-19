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

package models

import base.SpecBase
import generators.Generators
import models.reference.{Country, CountryCode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class SelectableListSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "countriesOfRoutingReads" - {
    "must read countries of routing as SelectableList" in {
      val json = Json.parse("""
          |[
          |  {
          |    "countryOfRouting": {
          |      "code": "IT",
          |      "description": "Italy"
          |    }
          |  },
          |  {
          |    "countryOfRouting": {
          |      "code": "FR",
          |      "description": "France"
          |    }
          |  }
          |]
          |""".stripMargin)

      val result = json.as[SelectableList[Country]](SelectableList.countriesOfRoutingReads)

      result mustBe SelectableList(
        Seq(
          Country(CountryCode("IT"), "Italy"),
          Country(CountryCode("FR"), "France")
        )
      )
    }
  }
}
