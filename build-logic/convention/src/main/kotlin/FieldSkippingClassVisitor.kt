/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor

/**
 * An ASM visitor that strips every FIELD from the classes it visits, leaving methods intact.
 *
 * Used to keep a class's API surface while discarding its state — see [Factory] for which classes are selected.
 */
class FieldSkippingClassVisitor(
    apiVersion: Int,
    nextClassVisitor: ClassVisitor,
) : ClassVisitor(apiVersion, nextClassVisitor) {

    // Returning null from this method will cause the ClassVisitor to strip all fields from the class.
    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?,
    ): FieldVisitor? = null

    /** Selects which classes [FieldSkippingClassVisitor] is applied to, from the names in [Parameters.classes]. */
    abstract class Factory : AsmClassVisitorFactory<Parameters> {

        private val excludedClasses
            get() = parameters.get().classes.get()

        override fun isInstrumentable(classData: ClassData): Boolean =
            classData.className in excludedClasses

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: org.objectweb.asm.ClassVisitor,
        ): org.objectweb.asm.ClassVisitor {
            return FieldSkippingClassVisitor(
                apiVersion = instrumentationContext.apiVersion.get(),
                nextClassVisitor = nextClassVisitor,
            )
        }
    }

    /** The instrumentation's inputs. */
    abstract class Parameters : InstrumentationParameters {
        /**
         * Fully-qualified names of the classes whose fields are stripped. A `SetProperty` so Gradle tracks it as a
         * task input and re-instruments when the set changes.
         */
        @get:Input
        abstract val classes: SetProperty<String>
    }
}
