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

import forms.locationOfGoods.PostalCodeFormProvider
import generators.Generators
import models.reference.Country
import models.{NormalMode, PostalCodeAddress, SelectableList}
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.PostalCodeAddressViewBehaviours
import views.html.locationOfGoods.PostalCodeView

class PostalCodeViewSpec extends PostalCodeAddressViewBehaviours with Generators {

  private val formProvider = new PostalCodeFormProvider()(phaseConfig)

  private val countryList = arbitrary[SelectableList[Country]].sample.value

  override val prefix: String = "locationOfGoods.postalCode"

  override def form: Form[PostalCodeAddress] = formProvider(prefix, countryList)

  def applyView(form: Form[PostalCodeAddress]): HtmlFormat.Appendable =
    injector.instanceOf[PostalCodeView].apply(form, lrn, NormalMode, countryList.values)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Location of goods")

  behave like pageWithHeading()

  behave like pageWithAddressInput()

  behave like pageWithSubmitButton("Save and continue")
}
