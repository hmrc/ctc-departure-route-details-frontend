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

import forms.SelectableFormProvider
import forms.SelectableFormProvider.OfficeFormProvider
import models.reference.CustomsOffice
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.locationOfGoods.CustomsOfficeIdentifierView

class CustomsOfficeIdentifierViewSpec extends InputSelectViewBehaviours[CustomsOffice] {

  private val formProvider = new OfficeFormProvider()

  override def form: Form[CustomsOffice] = formProvider.apply(prefix, SelectableList(values))

  override val field: String = formProvider.field

  override def applyView(form: Form[CustomsOffice]): HtmlFormat.Appendable =
    injector.instanceOf[CustomsOfficeIdentifierView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[CustomsOffice] = arbitraryCustomsOffice

  override val prefix: String = "locationOfGoods.customsOfficeIdentifier"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Location of goods")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("Enter the office location or code, like Dover or GB000060.")

  behave like pageWithSubmitButton("Save and continue")
}
