requirements: for all dropdown multi select boxes strato components shall be used, see https://developer.dynatrace.com/design/components/forms/Select/. the values shall be sorted alphabetically if not specified otherwise. add this to ui.md

                <FilterBar.Item
                    name="language"
                    label="Choose Language"
                    className={dropdownWrapperStyle}
                >
                    <LanguageFilter
                        value={selectedLanguages}
                        options={Object.values(Language).map((lang) => ({
                            label: formatLanguageLabel(lang),
                            value: lang
                        }))}
                        onChange={(values) => setSelectedLanguages(values)}
                    />
                </FilterBar.Item>