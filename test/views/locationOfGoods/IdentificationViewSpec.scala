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

import forms.EnumerableFormProvider
import models.{LocationOfGoodsIdentification, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.behaviours.RadioViewBehaviours
import views.html.locationOfGoods.IdentificationView

class IdentificationViewSpec extends RadioViewBehaviours[LocationOfGoodsIdentification] {

  override def form: Form[LocationOfGoodsIdentification] = new EnumerableFormProvider()(prefix, values)

  override def applyView(form: Form[LocationOfGoodsIdentification]): HtmlFormat.Appendable =
    injector.instanceOf[IdentificationView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  override val prefix: String = "locationOfGoods.identification"

  override def radioItems(fieldId: String, checkedValue: Option[LocationOfGoodsIdentification] = None): Seq[RadioItem] =
    values.toRadioItems(fieldId, checkedValue)

  override def values: Seq[LocationOfGoodsIdentification] = Seq(
    LocationOfGoodsIdentification("V", "CustomsOfficeIdentifier"),
    LocationOfGoodsIdentification("X", "EoriNumber"),
    LocationOfGoodsIdentification("Y", "AuthorisationNumber"),
    LocationOfGoodsIdentification("U", "UnlocodeIdentifier"),
    LocationOfGoodsIdentification("W", "CoordinatesIdentifier"),
    LocationOfGoodsIdentification("Z", "AddressIdentifier"),
    LocationOfGoodsIdentification("T", "PostalCode")
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Location of goods")

  behave like pageWithHeading()

  behave like pageWithRadioItems(hintTextPrefix = Some(prefix))

  behave like pageWithSubmitButton("Save and continue")
}
