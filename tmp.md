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

add to the requirements documents all dropdown multi select boxes shall have a title,
eg Depot, Country, Branch etc and the title shall be visible when the dropdown is closed. 
The title shall be placed above the dropdown and aligned to the left.
The title shall be in the same font and size as the labels of the other filter items in the
filter bar. The title shall not be truncated and should be fully visible at all times. 
in the field "Choose Language" the title is "Language".
