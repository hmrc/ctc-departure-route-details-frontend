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

package pages.exit

import pages.behaviours.PageBehaviours
import pages.exit.index.OfficeOfExitPage

class AddCustomsOfficeOfExitYesNoPageSpec extends PageBehaviours {

  "AddCustomsOfficeOfExitYesNoPage" - {

    beRetrievable[Boolean](AddCustomsOfficeOfExitYesNoPage)

    beSettable[Boolean](AddCustomsOfficeOfExitYesNoPage)

    beRemovable[Boolean](AddCustomsOfficeOfExitYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must remove exit section" in {
          forAll(arbitraryOfficeOfExitAnswers(emptyUserAnswers, index)) {
            ua =>
              val preChange = ua
                .setValue(AddCustomsOfficeOfExitYesNoPage, true)

              preChange.get(OfficeOfExitPage(index)) must be(defined)

              val postChange = preChange.setValue(AddCustomsOfficeOfExitYesNoPage, false)

              postChange.get(OfficeOfExitPage(index)) mustNot be(defined)
          }
        }
      }
    }
  }
}
