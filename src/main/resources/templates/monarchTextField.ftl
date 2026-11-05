<#assign builder = includeMetaData!false>
<#assign hidden = (element.properties.hiddenField!'') == 'true'>
<#assign readonly = readonly!((element.properties.readonly!'') == 'true')>
<#if hidden>
    <#if builder>
        <#-- Use Joget's native invisible-element presentation, including its theme and icon. -->
        <div class="form-cell" data-cbuilder-element-invisible="" ${elementMetaData!}>
            <label class="label" for="${elementParamName!?html}">${element.properties.id!?html}</label>
            <input id="${elementParamName!?html}" name="${elementParamName!?html}" type="text" value="${value!?html}" readonly="readonly" />
        </div>
    <#else>
        <input id="${elementParamName!?html}" name="${elementParamName!?html}" type="hidden" value="${value!?html}" />
    </#if>
<#else>
    <div class="form-cell" data-monarch-instance="${renderInstanceId!?html}" ${elementMetaData!}>
        <label field-tooltip="${elementParamName!?html}" class="label" for="${elementParamName!?html}">
            ${element.properties.label!}
            <span class="form-cell-validator">${decoration!}</span>
            <#if error??><span id="${elementParamName!?html}_error" class="form-error-message">${error}</span></#if>
        </label>
        <#if readonly && (element.properties.readonlyLabel!'') == 'true'>
            <div class="form-cell-value"><span>${valueLabel!?html}</span></div>
            <input id="${elementParamName!?html}" name="${elementParamName!?html}" class="textfield_${element.properties.elementUniqueKey!?html}" type="hidden" value="${value!?html}" />
        <#else>
            <input id="${elementParamName!?html}"
                   name="${elementParamName!?html}"
                   class="monarch-textfield-input textfield_${element.properties.elementUniqueKey!?html}<#if error??> form-error-cell</#if>"
                   type="text"
                   placeholder="${element.properties.placeholder!?html}"
                   <#if (element.properties.size!'')?has_content>size="${element.properties.size?html}"</#if>
                   value="${value!?html}"
                   <#if (element.properties.maxlength!'')?has_content>maxlength="${element.properties.maxlength?html}"</#if>
                   <#if error??>aria-invalid="true" aria-describedby="${elementParamName!?html}_error"</#if>
                   <#if readonly>readonly="readonly"</#if> />
        </#if>
        <#if !builder && !readonly && ((element.properties.style!'')?has_content || (element.properties.lettercase!'')?has_content || (element.properties.symbols!'') == 'true')>
            <script type="text/javascript">
                (function() {
                    // Scope to this cell so repeated fields and subforms cannot affect one another.
                    $(function() {
                        var $input = $('[data-monarch-instance="${renderInstanceId!?js_string}"]').children('input.monarch-textfield-input');
                        var format = '${(element.properties.style!'')?js_string}';
                        var lettercase = '${(element.properties.lettercase!'')?js_string}';
                        // Numeric formatting owns punctuation such as decimal separators and currency.
                        var disableSymbols = '${(element.properties.symbols!'')?js_string}' === 'true' && format === '';

                        function normalize(event) {
                            if (event.originalEvent && event.originalEvent.isComposing) {
                                return;
                            }
                            var value = this.value;
                            if (disableSymbols) {
                                value = value.replace(/[^a-zA-Z0-9\s]/g, '');
                            }
                            if (lettercase === 'uppercase') {
                                value = value.toUpperCase();
                            } else if (lettercase === 'lowercase') {
                                value = value.toLowerCase();
                            } else if (lettercase === 'capitalize') {
                                value = value.toLowerCase().replace(/(^|\s)\S/g, function(character) {
                                    return character.toUpperCase();
                                });
                            }
                            if (value !== this.value) {
                                var start = this.selectionStart;
                                var end = this.selectionEnd;
                                var direction = this.selectionDirection;
                                this.value = value;
                                if (start !== null && end !== null) {
                                    this.setSelectionRange(Math.min(start, value.length), Math.min(end, value.length), direction);
                                }
                            }
                        }

                        if (lettercase !== '' || disableSymbols) {
                            $input.off('.monarchTextField').on('input.monarchTextField change.monarchTextField compositionend.monarchTextField', normalize);
                        }
                        if (format !== '') {
                            function initializeNumberFormatting() {
                                $input.numberFormatting({
                                    format: format,
                                    numOfDecimal: '${(element.properties.numOfDecimal!'')?js_string}',
                                    useThousandSeparator: '${(element.properties.useThousandSeparator!'')?js_string}',
                                    prefix: '${(element.properties.prefix!'')?js_string}',
                                    postfix: '${(element.properties.postfix!'')?js_string}'
                                });
                            }
                            if ($.fn.numberFormatting) {
                                initializeNumberFormatting();
                            } else {
                                // Share a single pending script load across numeric fields.
                                var loading = window.monarchTextFieldNumberFormatting;
                                if (!loading) {
                                    loading = $.getScript('${(request.contextPath!'')?js_string}/plugin/org.joget.apps.form.lib.TextField/js/jquery.numberFormatting.js');
                                    window.monarchTextFieldNumberFormatting = loading;
                                    loading.fail(function() {
                                        window.monarchTextFieldNumberFormatting = null;
                                    });
                                }
                                loading.done(initializeNumberFormatting);
                            }
                        }
                    });
                })();
            </script>
        </#if>
    </div>
</#if>
