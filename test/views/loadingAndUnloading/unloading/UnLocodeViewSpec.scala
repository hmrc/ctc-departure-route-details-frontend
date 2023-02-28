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

package views.loadingAndUnloading.unloading

import models.NormalMode
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours

class UnLocodeViewSpec extends InputSelectViewBehaviours[UnLocode] {

  override def form: Form[UnLocode] = new UnLocodeFormProvider()(prefix, UnLocodeList(values))

  override def applyView(form: Form[UnLocode]): HtmlFormat.Appendable =
    injector.instanceOf[UnLocodeView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[UnLocode] = arbitraryUnLocode

  override val prefix: String = "routeDetails.loadingAndUnloading.unloading.unLocode"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Place of unloading")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("Enter the code, like DEBER or ESMAD.")

  behave like pageWithSubmitButton("Save and continue")
}
