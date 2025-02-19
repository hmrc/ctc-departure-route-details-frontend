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

package views.locationOfGoods

import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.locationOfGoods.AddLocationOfGoodsView

class AddLocationOfGoodsViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AddLocationOfGoodsView].apply(form, lrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "locationOfGoods.addLocationOfGoods"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Location of goods")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This is where Customs will carry out any physical checks.")

  behave like pageWithHint("Adding a location of goods is optional.")

  behave like pageWithContent("p", "For goods not present at a customs office, this is their location at the time they are declared.")

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
