/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.kotlin

import toothpick.Scope
import toothpick.config.Module

class KTPScope(private val scope: Scope) : Scope by scope {

    override fun inject(obj: Any) {
        KTP.delegateNotifier.notifyDelegates(obj, scope)
    }

    override fun installModules(vararg modules: Module): Scope {
        scope.installModules(*modules)
        return this
    }

    override fun supportScopeAnnotation(scopeAnnotationClass: Class<out Annotation>): Scope {
        scope.supportScopeAnnotation(scopeAnnotationClass)
        return this
    }

    override fun getParentScope(): Scope {
        return KTPScope(scope.parentScope)
    }

    override fun getParentScope(scopeAnnotationClass: Class<*>): Scope {
        return KTPScope(scope.getParentScope(scopeAnnotationClass))
    }

    override fun getRootScope(): Scope {
        return KTPScope(scope.rootScope)
    }
}
