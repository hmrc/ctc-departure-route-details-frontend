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

package views.exit.index

import forms.YesNoFormProvider
import generators.Generators
import models.Mode
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.exit.RemoveOfficeOfExitViewModel
import views.behaviours.YesNoViewBehaviours
import views.html.exit.index.ConfirmRemoveOfficeOfExitView

class ConfirmRemoveOfficeOfExitViewSpec extends YesNoViewBehaviours with Generators {

  private lazy val officeOfExit = arbitraryCustomsOffice.arbitrary.sample.value

  private val mode = arbitrary[Mode].sample.value

  private val viewModel = new RemoveOfficeOfExitViewModel(Some(officeOfExit))

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[ConfirmRemoveOfficeOfExitView].apply(form, lrn, index, mode, viewModel)(fakeRequest, messages)

  override val prefix: String = "exit.index.confirmRemoveOfficeOfExit"

  behave like pageWithTitle(officeOfExit.name)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Office of exit for transit")

  behave like pageWithHeading(officeOfExit.name)

  behave like pageWithRadioItems(args = Seq(officeOfExit.name))

  behave like pageWithSubmitButton("Save and continue")

  "when no office name is present in user answers" - {
    val defaultPrefix = s"$prefix.default"
    val viewModel     = new RemoveOfficeOfExitViewModel(None)
    val form          = new YesNoFormProvider()(defaultPrefix)
    val view          = injector.instanceOf[ConfirmRemoveOfficeOfExitView].apply(form, lrn, index, mode, viewModel)(fakeRequest, messages)
    val doc           = parseView(view)

    behave like pageWithTitle(doc, defaultPrefix)

    behave like pageWithHeading(doc, defaultPrefix)
  }
}
